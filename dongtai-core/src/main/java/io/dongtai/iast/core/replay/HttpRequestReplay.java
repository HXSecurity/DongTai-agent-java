package io.dongtai.iast.core.replay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.utils.base64.Base64Decoder;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.IastReplayModel;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.HashMap;

/**
 * 根据字符串格式的header头及cookie信息生成重放请求
 * 问题：
 * 1. 触发sql查询时，未出现多余的身份验证信息（Cookie)，此时作何处理？
 * 方案：
 * - 忽略当前请求的越权检测，将导致检测结果的不稳定（偶发性检测，不建议）
 * - 临时保存当前请求，当出现多个cookie时，替换cookie进行检查（数据如何存储？）
 * 2. 重放权限信息时，全部重放还是重放其中的一个？
 * 背景；如果重放多个，将导致网络IO增加，可能影响服务器的正常运行，如何解决该问题？如果重放一个，可能导致漏洞检出不准确的问题
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HttpRequestReplay implements Runnable {
    private final StringBuilder replayRequestRaw;

    public HttpRequestReplay(StringBuilder replayRequestRaw) {
        this.replayRequestRaw = replayRequestRaw;
    }

    /**
     * 发起重放请求
     */
    private static void doReplay(IastReplayModel replayModel) {
        try {
            HashMap<String, String> headers = splitHeaderStringToHashmap(replayModel.getRequestHeader());
            headers.put("dongtai-replay-id", String.valueOf(replayModel.getReplayId()));
            headers.put("dongtai-relation-id", String.valueOf(replayModel.getRelationId()));
            headers.put("dongtai-replay-type", String.valueOf(replayModel.getReplayType()));
            headers.remove("content-length");
            headers.remove("transfer-encoding");
            headers.remove(ContextManager.getHeaderKey());

            String url = replayModel.getFullUrl();
            if (url != null) {
                DongTaiLog.debug("Do request replay: {} {}, data={}, header={}",
                        replayModel.getRequestMethod(), url, replayModel.getRequestBody(), headers.toString());
                HttpClientUtils.sendReplayRequest(replayModel.getRequestMethod(), url, replayModel.getRequestBody(), headers);
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("REPLAY_REQUEST_FAILED"), replayModel.getFullUrl(), e);
        }
    }

    private static HashMap<String, String> splitHeaderStringToHashmap(String originalHeaders) {
        HashMap<String, String> headers = new HashMap<String, String>(32);
        byte[] headerRaw = Base64Decoder.decodeBase64FromString(originalHeaders);
        if (headerRaw == null) {
            return headers;
        }

        String decodeHeaders = new String(headerRaw);
        String[] headerItems = decodeHeaders.trim().split("\n");
        for (String item : headerItems) {
            int splitCharIndex = item.indexOf(":");
            if (splitCharIndex > 0) {
                String key = item.substring(0, splitCharIndex);
                String value = item.substring(splitCharIndex + 1);
                headers.put(key, value);
            }
        }

        return headers;
    }

    @Override
    public void run() {
        try {
            JSONObject resp = JSON.parseObject(replayRequestRaw.toString());
            Integer statusCode = (Integer) resp.get("status");
            if (statusCode != 201) {
                return;
            }

            String data = resp.get("data").toString();
            if ("[]".equals(data)) {
                return;
            }

            JSONArray replayRequests = (JSONArray) resp.get("data");
            for (int index = 0, total = replayRequests.size(); index < total; index++) {
                JSONObject replayRequest = (JSONObject) replayRequests.get(index);
                IastReplayModel replayModel = new IastReplayModel(
                        replayRequest.get("method"),
                        replayRequest.get("uri"),
                        replayRequest.get("params"),
                        replayRequest.get("body"),
                        replayRequest.get("header"),
                        replayRequest.get("id"),
                        replayRequest.get("relation_id"),
                        replayRequest.get("replay_type"));
                if (replayModel.isValid()) {
                    doReplay(replayModel);
                }
            }
        } catch (Throwable ignore) {
        }
    }
}
