package com.secnium.iast.core.handler.vulscan.overpower;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.Asserts;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

/**
 * Auth凭证信息生命周期管理
 * - 新增、替换
 *
 * @author dongzhiyong@huoxian.cn
 */
public class AuthInfoManager {
    private static final Logger LOGGER = LogUtils.getLogger(LoginLogicRecognize.class);
    private static final String SET_COOKIE = "Set-Cookie";

    /**
     * 处理 set-cookie 逻辑，如果当前请求是登陆逻辑，则创建初识cookie并发送cookie报告至远程服务器
     * todo: 进行cookie的替换，question: 同一个请求中多次触发Set-Cookie时，如何处理？先缓存当前cookie，统一插入？顺序是否有影响
     *
     * @param name  属性名称，用于判断是否为Set-Cookie，如果是Set-Cookie则执行cookie处理
     * @param value 属性值
     */
    public static void handleSetCookieAction(Object name, Object value) {
        if (SET_COOKIE.equals(name)) {
            String cookieFullValue = (String) value;
            String cookieValue = cookieFullValue.substring(0, cookieFullValue.indexOf(";"));
            if (EngineManager.getIsLoginLogic()) {
                AuthInfoCache.addAuthInfoToCache(cookieValue);
                //todo: cookie保存至ThreadLocal，等待HTTP方法退出时，将数据发送至IAST服务器端
            } else {
                String cookie = EngineManager.REQUEST_CONTEXT.getCookieValue();
                if (cookie != null) {
                    // 更新cookie
                    String updatedCookie = splitAndReplaceCookie(cookie, cookieValue);
                    AuthInfoCache.updateAuthInfo(cookie, updatedCookie);
                    String report = generateAuthUpdateReport(cookie, updatedCookie);
                    EngineManager.sendNewReport(report);
                }
            }
        }
    }

    /**
     * 处理登陆时，存在cookie的情况，将sql查询语句、url和请求的cookie保存，等待HTTP方法退出时将数据发送至IAST服务器端
     * <p>
     * fixme: 报告声称和数据发送逻辑迁移至HTTP Service方法退出
     *
     * @param jdbcClassName JDBC实现类的名称
     * @param cookie        cookie值，如果为null则抛出异常
     * @param sqlStatement  查询的sql语句
     */
    public static void handleAddCookieAction(String jdbcClassName, String cookie, String sqlStatement) {
        Asserts.NOT_NULL("auth.value", cookie);
        AuthInfoCache.addAuthInfoToCache(cookie);
        String report = generateAuthAddReport(jdbcClassName, cookie, sqlStatement);
        EngineManager.sendNewReport(report);
        LOGGER.debug(report);
    }

    /**
     * 生成新增auth凭证信息报告
     *
     * @param jdbcClassName 处理sql语句的JDBC实现类
     * @param cookie        新增加的凭证信息
     * @param sqlStatement  登陆相关的sql查询语句
     * @return auth凭证信息报告
     */
    private static String generateAuthAddReport(String jdbcClassName, String cookie, String sqlStatement) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_AUTH_ADD);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, EngineManager.REQUEST_CONTEXT.get().getContextPath());
        detail.put(ReportConstant.COMMON_SERVER_NAME, EngineManager.REQUEST_CONTEXT.get().getServerName());
        detail.put(ReportConstant.COMMON_SERVER_PORT, EngineManager.REQUEST_CONTEXT.get().getServerPort());
        detail.put(ReportConstant.COMMON_REMOTE_IP, EngineManager.REQUEST_CONTEXT.get().getRemoteIp());
        detail.put(ReportConstant.COMMON_HTTP_URL, EngineManager.REQUEST_CONTEXT.get().getRequestURL());
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, EngineManager.REQUEST_CONTEXT.get().getQueryString());
        detail.put(ReportConstant.COMMON_HTTP_BODY, EngineManager.REQUEST_CONTEXT.get().getCachedBody());
        detail.put(ReportConstant.AUTH_ADD_JDBC_CLASS, jdbcClassName);
        detail.put(ReportConstant.AUTH_ADD_VALUE, cookie);
        detail.put(ReportConstant.AUTH_ADD_SQL_STATEMENT, sqlStatement);

        return report.toString();
    }

    /**
     * 生成auth凭证更新的报告
     *
     * @param originalCookie 原始凭证信息
     * @param updatedCookie  更新后的凭证信息
     * @return 凭证更新的报告
     */
    private static String generateAuthUpdateReport(String originalCookie, String updatedCookie) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_AUTH_UPDATE);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.COMMON_APP_NAME, EngineManager.REQUEST_CONTEXT.get().getContextPath());
        detail.put(ReportConstant.COMMON_SERVER_NAME, EngineManager.REQUEST_CONTEXT.get().getServerName());
        detail.put(ReportConstant.COMMON_SERVER_PORT, EngineManager.REQUEST_CONTEXT.get().getServerPort());
        detail.put(ReportConstant.COMMON_REMOTE_IP, EngineManager.REQUEST_CONTEXT.get().getRemoteIp());
        detail.put(ReportConstant.COMMON_HTTP_URL, EngineManager.REQUEST_CONTEXT.get().getRequestURL());
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, EngineManager.REQUEST_CONTEXT.get().getQueryString());
        detail.put(ReportConstant.COMMON_HTTP_BODY, EngineManager.REQUEST_CONTEXT.get().getCachedBody());
        detail.put(ReportConstant.AUTH_UPDATE_ORIGINA, originalCookie);
        detail.put(ReportConstant.AUTH_UPDATE_UPDATED, updatedCookie);

        return report.toString();
    }

    public static String splitAndReplaceCookie(String cookie, String updatedCookie) {
        String[] cookies = cookie.split(";");
        String[] modifyCookies = cookie.split(";");
        String updatedCookieName = updatedCookie.split("=")[0];
        for (int i = 0; i < cookies.length; i++) {
            String tempCookieName = cookies[i].split("=")[0];
            if (tempCookieName.trim().equals(updatedCookieName.trim())) {
                modifyCookies[i] = updatedCookie.replace(updatedCookieName, tempCookieName);
                break;
            }
        }
        return StringUtils.join(modifyCookies, ";");
    }
}
