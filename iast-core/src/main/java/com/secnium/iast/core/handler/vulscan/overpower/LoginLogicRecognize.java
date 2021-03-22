package com.secnium.iast.core.handler.vulscan.overpower;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.util.Asserts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 识别登陆逻辑，用于判断未授权访问与越权访问
 *
 * @author dongzhiyong@huoxian.cn
 */
public class LoginLogicRecognize {

    /**
     * 登陆相关URI的判断标准，通过URI进行判断，可能存在判断失败的情况，需要补充足够多的数据集，包括：URI/请求参数等
     */
    private static final Pattern LOGIN_PATTERN = Pattern.compile(".*?login.*?");
    private static final Pattern PHONE_PATTERN = Pattern.compile(".*?(phone).*?");
    private static final Pattern USERNAME_PATTERN = Pattern.compile(".*?(user|username|userid|user_id).*?");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(".*?(password|pass).*?");

    /**
     * 检查当前uri是否为登陆uri
     *
     * @param uri 当前请求的uri
     * @return true-登陆接口，false-非登陆接口
     */
    public static boolean isLoginUrl(String uri) {
        Matcher matcher = LOGIN_PATTERN.matcher(uri);
        return matcher.find();
    }

    /**
     * 检查当前sql语句是否为登陆相关的sql语句
     *
     * @param sql 当前查询的sql语句
     * @return true-登陆查询，false-非登陆查询
     */
    public static boolean isLoginSqlQuery(String sql) {
        // 提取where条件的部分，检测是否为登陆相关的sql
        Matcher userMatcher = USERNAME_PATTERN.matcher(sql);
        Matcher passMatcher = PASSWORD_PATTERN.matcher(sql);

        return (userMatcher.find() && passMatcher.find()) || isLoginByName(sql);
    }

    public static boolean isLoginByName(String sql) {
        return USERNAME_PATTERN.matcher(sql).find() || PHONE_PATTERN.matcher(sql).find();
    }

    /**
     * 处理登陆逻辑识别，检查当前sql是否为用户登陆逻辑
     *
     * @param jdbcClassName JDBC实例化类的名称
     * @param sqlStatement  执行的sql语句
     */
    public static void handleLoginLogicRecognize(String jdbcClassName, String sqlStatement) {
        try {
            Asserts.NOT_NULL("sqlstatement", sqlStatement);
            if (EngineManager.getIsLoginLogicUrl()) {
                if (LoginLogicRecognize.isLoginSqlQuery(sqlStatement)) {
                    EngineManager.setIsLoginLogic();
                    if (EngineManager.getIsLoginLogic()) {
                        EngineManager.setLogined();
                    }

                    //fixme: 登陆信息、查询语句和cookie信息何时发送至IAST服务器端？
                    // 1. 将登陆语句、jdbc实现类和当前请求的cookie信息临时保存，等待set-cookie时再进行处理？
                    //        可能存在的问题：如果后续未进行set-cookie怎么办？
                    // 2. 将登陆语句、jdbc实现类和当前请求的cookie信息直接发送至服务器端，后续set-cookie时，如何进行关联？
                    // 3. 离开http请求时发送至服务器端（最好的方案）
                    String cookie = EngineManager.REQUEST_CONTEXT.getCookieValue();
                    AuthInfoManager.handleAddCookieAction(jdbcClassName, cookie, sqlStatement);
                }
            }
        } catch (Exception ignore) {
            ;
        }
    }
}
