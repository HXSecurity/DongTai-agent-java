package com.secnium.iast.core.handler.vulscan.overpower;

import org.junit.Test;

public class AuthInfoManagerTest {


    @Test
    public void testSplitAndReplaceCookie() {
        String cookie = "Hm_lvt_bdff1c1dcce971c3d986f9be0921a0ee=1597126590,1597196300,1597286523,1598406121; Idea-f5522474=037429db-a2e8-4260-a8c3-c116995217b8; sessionid=qgliqomx7j7h4tpeokqjd6zwnc2ll18b; JSESSIONID=9740671BB9D74BEBE384B926BF8C12CD";
        String updatedCookie = "JSESSIONID=8659C4E1A62E4F13FE251D02170B8C7A";
        String newCookie = AuthInfoManager.splitAndReplaceCookie(cookie, updatedCookie);
        System.out.println("cookie = " + cookie);
        System.out.println("updatedCookie = " + updatedCookie);
        System.out.println("newCookie = " + newCookie);
    }
}
