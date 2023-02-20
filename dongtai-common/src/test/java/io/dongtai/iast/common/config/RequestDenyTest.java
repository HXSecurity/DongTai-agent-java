package io.dongtai.iast.common.config;

import io.dongtai.iast.common.config.RequestDeny.Operator;
import io.dongtai.iast.common.config.RequestDeny.TargetType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RequestDenyTest {
    @Test
    public void testMatch() {
        String url = "https://foo.bar/baz?key=val";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("key1", "value1");
        headers.put("key2", "value2");

        Map<RequestDeny, Boolean> tests = new HashMap<RequestDeny, Boolean>() {{
            put(new RequestDeny(TargetType.URL, Operator.EQUAL, "https://foo.bar/baz"), true);
            put(new RequestDeny(TargetType.URL, Operator.EQUAL, "https://foo.bar/baz2"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_EQUAL, "https://foo.bar/baz"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_EQUAL, "https://foo.bar/baz2"), true);
            put(new RequestDeny(TargetType.URL, Operator.EQUAL, "https://foo.bar/BAZ"), true);
            put(new RequestDeny(TargetType.URL, Operator.EQUAL, "https://foo.bar/BAZ2"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_EQUAL, "https://foo.bar/BAZ"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_EQUAL, "https://foo.bar/BAZ2"), true);
            put(new RequestDeny(TargetType.URL, Operator.CONTAIN, "https"), true);
            put(new RequestDeny(TargetType.URL, Operator.CONTAIN, "foo.bar"), true);
            put(new RequestDeny(TargetType.URL, Operator.CONTAIN, "/baz"), true);
            put(new RequestDeny(TargetType.URL, Operator.CONTAIN, "key"), false);
            put(new RequestDeny(TargetType.URL, Operator.CONTAIN, "HTTPS"), true);
            put(new RequestDeny(TargetType.URL, Operator.CONTAIN, "KEY"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_CONTAIN, "https"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_CONTAIN, "foo.bar"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_CONTAIN, "/baz"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_CONTAIN, "key"), true);
            put(new RequestDeny(TargetType.URL, Operator.NOT_CONTAIN, "HTTPS"), false);
            put(new RequestDeny(TargetType.URL, Operator.NOT_CONTAIN, "KEY"), true);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.EXISTS, "key1"), true);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.EXISTS, "key3"), false);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.EXISTS, "KEY1"), true);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.EXISTS, "KEY3"), false);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.NOT_EXISTS, "key1"), false);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.NOT_EXISTS, "key3"), true);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.NOT_EXISTS, "KEY1"), false);
            put(new RequestDeny(TargetType.HEADER_KEY, Operator.NOT_EXISTS, "KEY3"), true);
        }};

        for (Map.Entry<RequestDeny, Boolean> entry : tests.entrySet()) {
            boolean matched = entry.getKey().match(url, headers);
            Assert.assertEquals("match " + entry.getKey(), entry.getValue(), matched);
        }
    }
}