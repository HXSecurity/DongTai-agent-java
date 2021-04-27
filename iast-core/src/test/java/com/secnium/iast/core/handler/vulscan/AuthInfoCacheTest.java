package com.secnium.iast.core.handler.vulscan;

import com.secnium.iast.core.handler.vulscan.overpower.AuthInfoCache;
import org.junit.Test;

public class AuthInfoCacheTest {
    @Test
    public void testDuplicateItem() {
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233213"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233213"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233213"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233213"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233213"));
        System.out.println("AuthInfoCache.size() = " + AuthInfoCache.getSize());
    }

    @Test
    public void testRandomAccessItem() {
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233213"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233214"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233215"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233213"));
        System.out.println(
                "AuthInfoCache.addAuthInfoToCache(\"1233213\") = " + AuthInfoCache.addAuthInfoToCache("1233214"));
        System.out.println("AuthInfoCache.size() = " + AuthInfoCache.getSize());

        System.out.println("true = " + AuthInfoCache.getAnotherCookie("1233213"));
    }

    @Test
    public void testUpdateItem() {
        // AuthInfoCache.addAuthInfoToCache("1233213");
        // AuthInfoCache.addAuthInfoToCache("12323123");
        // AuthInfoCache.updateAuthInfo("1233213", "1233214");
        // AuthInfoCache.displayCookie();
        System.out.println("AuthInfoCache.size() = " + AuthInfoCache.getSize());
    }
}
