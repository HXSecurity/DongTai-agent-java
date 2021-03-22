package com.secnium.iast.core.handler.vulscan;

import com.secnium.iast.core.handler.vulscan.overpower.LoginLogicRecognize;
import org.junit.Test;

public class LoginLogicRecognizeTest {
    @Test
    public void isLoginUrl() {
        String url = "https://127.0.0.1:8080/admin/login";
        boolean isLoginLogic = LoginLogicRecognize.isLoginUrl(url);
        System.out.println("isLoginLogic = " + isLoginLogic);
    }

    @Test
    public void isLoginQuery() {
        String sql = "select id from user='admin' and password='12341234'";
        boolean isLoginQuery = LoginLogicRecognize.isLoginSqlQuery(sql);
        System.out.println("isLoginQuery = " + isLoginQuery);
    }


}
