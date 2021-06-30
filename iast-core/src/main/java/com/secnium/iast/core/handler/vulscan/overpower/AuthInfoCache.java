package com.secnium.iast.core.handler.vulscan.overpower;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.util.Asserts;
import com.secnium.iast.core.util.LogUtils;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存所有的cookie
 * todo: 如何做COOKIE生命周期管理？
 * - 用户请求，带cookie，提取cookie，放入cookie池中（创建）
 * - 遇到set-Cookie，将set-Cookie的内容加入当前请求对应的Cookie的Cookie池（更新）
 * - 一段时间之后，释放Cookie
 *
 * @author dongzhiyong@huoxian.cn
 */
public class AuthInfoCache extends ConcurrentHashMap<Integer, Object> {

    private static final AuthInfoCache INSTANCE = new AuthInfoCache();

    /**
     * 将用户凭证信息发送至凭证缓存池
     *
     * @param value 身份权限相关的字段
     * @return true: 成功添加到cache中；false：cache中已存在，不需要添加
     */
    public static synchronized boolean addAuthInfoToCache(Object value) {
        if (value != null) {
            return INSTANCE.put(value.hashCode(), value) == null;
        }
        return false;
    }

    /**
     * 更新鉴权信息
     * fixme: 当前使用全局替换，非正确方法，后续精确识别具体的字段进行替换
     *
     * @param originalAuth 原始请求中的鉴权信息
     * @param updatedAuth  更新后的鉴权信息
     */
    public static synchronized void updateAuthInfo(Object originalAuth, Object updatedAuth) {
        Asserts.NOT_NULL("original.auth", originalAuth);
        Asserts.NOT_NULL("updated.auth", updatedAuth);
        for (Iterator<Entry<Integer, Object>> it = INSTANCE.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Object> item = it.next();
            //... todo with item
            if (item.getKey().equals(originalAuth.hashCode())) {
                it.remove();
                break;
            }
        }
        addAuthInfoToCache(updatedAuth);
        logger.debug("\n===>>> url: {}\n===>>> original cookie: {}\n===>>> updated cookie：{}", EngineManager.REQUEST_CONTEXT.get().get("requestURL").toString(), originalAuth, updatedAuth);
    }

    public static synchronized int getSize() {
        return INSTANCE.size();
    }

    private AuthInfoCache() {
        super(128);
    }

    public static synchronized String getAnotherCookie(String cookie) {
        Object newCookie = cookie;
        for (Entry<Integer, Object> entry : INSTANCE.entrySet()) {
            if (!entry.getKey().equals(cookie.hashCode())) {
                newCookie = entry.getValue();
                break;
            }
        }
        return (String) newCookie;
    }

    public static synchronized boolean isNeededCheckOverPower() {
        return getSize() >= 2;
    }

    public static synchronized void displayCookie() {
        for (Entry<Integer, Object> entry : INSTANCE.entrySet()) {
            logger.info("===>>> cookie is {}", entry.getValue());
        }
    }

    private static final Logger logger = LogUtils.getLogger(AuthInfoCache.class);

}
