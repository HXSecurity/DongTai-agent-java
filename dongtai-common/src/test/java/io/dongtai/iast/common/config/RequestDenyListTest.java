package io.dongtai.iast.common.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class RequestDenyListTest {
    @Test
    public void testMatch() {
        final String url = "https://foo.bar/baz?key=val";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("key1", "value1");
        headers.put("key2", "value2");

        final RequestDeny urlMatch = new RequestDeny(RequestDeny.TargetType.URL,
                RequestDeny.Operator.CONTAIN, "foo");
        final RequestDeny urlNotMatch = new RequestDeny(RequestDeny.TargetType.URL,
                RequestDeny.Operator.NOT_CONTAIN, "foo");
        final RequestDeny headerKeyMatch = new RequestDeny(RequestDeny.TargetType.HEADER_KEY,
                RequestDeny.Operator.EXISTS, "key1");
        final RequestDeny headerKeyNotMatch = new RequestDeny(RequestDeny.TargetType.HEADER_KEY,
                RequestDeny.Operator.NOT_EXISTS, "key1");

        Map<RequestDenyList, Boolean> tests = new HashMap<RequestDenyList, Boolean>(){{
            RequestDenyList requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(urlMatch));
            put(requestDenyList, true);
            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(headerKeyMatch));
            put(requestDenyList, true);
            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(urlNotMatch));
            put(requestDenyList, false);
            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(headerKeyNotMatch));
            put(requestDenyList, false);

            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Arrays.asList(urlMatch, headerKeyMatch));
            put(requestDenyList, true);
            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Arrays.asList(urlMatch, headerKeyNotMatch));
            put(requestDenyList, false);
            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Arrays.asList(urlNotMatch, headerKeyMatch));
            put(requestDenyList, false);
            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Arrays.asList(urlNotMatch, headerKeyNotMatch));
            put(requestDenyList, false);

            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(urlMatch));
            requestDenyList.addRule(Collections.singletonList(headerKeyMatch));
            put(requestDenyList, true);

            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(urlMatch));
            requestDenyList.addRule(Collections.singletonList(headerKeyNotMatch));
            put(requestDenyList, true);

            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(urlNotMatch));
            requestDenyList.addRule(Collections.singletonList(headerKeyMatch));
            put(requestDenyList, true);

            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Collections.singletonList(urlNotMatch));
            requestDenyList.addRule(Collections.singletonList(headerKeyNotMatch));
            put(requestDenyList, false);

            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Arrays.asList(urlMatch, headerKeyMatch));
            requestDenyList.addRule(Collections.singletonList(headerKeyNotMatch));
            put(requestDenyList, true);

            requestDenyList = new RequestDenyList();
            requestDenyList.addRule(Arrays.asList(urlMatch, headerKeyNotMatch));
            requestDenyList.addRule(Collections.singletonList(urlNotMatch));
            put(requestDenyList, false);
        }};

        for (Map.Entry<RequestDenyList, Boolean> entry : tests.entrySet()) {
            boolean matched = entry.getKey().match(url, headers);
            Assert.assertEquals("match " + entry.getKey(), entry.getValue(), matched);
        }
    }
}