package io.dongtai.iast.common.config;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class ConfigBuilderTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testGetConfigAndUpdate() {
        JSONObject configJson;
        String configString;
        ConfigBuilder builder = ConfigBuilder.getInstance();
        boolean reportResponseBody;
        int reportMaxMethodPoolSize;
        RequestDenyList requestDenyList;

        // default
        reportResponseBody = ((Config<Boolean>)builder.getConfig(ConfigKey.REPORT_RESPONSE_BODY)).get();
        Assert.assertTrue("REPORT_RESPONSE_BODY default", reportResponseBody);
        reportMaxMethodPoolSize = ((Config<Integer>)builder.getConfig(ConfigKey.REPORT_MAX_METHOD_POOL_SIZE)).get();
        Assert.assertEquals("REPORT_MAX_METHOD_POOL_SIZE default", 5000, reportMaxMethodPoolSize);
        requestDenyList = ((Config<RequestDenyList>)builder.getConfig(ConfigKey.REQUEST_DENY_LIST)).get();
        Assert.assertNull("REQUEST_DENY_LIST default", requestDenyList);

        // update
        configString = "{\"gather_res_body\": false}";
        configJson = new JSONObject(configString);
        builder.update(configJson);
        configString = "{\"method_pool_max_length\": 1000}";
        configJson = new JSONObject(configString);
        builder.update(configJson);
        configString = "{\"blacklist_rules\": [[{\"target_type\": \"HEADER_KEY\", \"operator\": \"EXISTS\", \"value\": \"key1\"}]]}";
        configJson = new JSONObject(configString);
        builder.update(configJson);

        RequestDenyList expectRequestDenyList = new RequestDenyList();
        RequestDeny headerKeyMatch = new RequestDeny(RequestDeny.TargetType.HEADER_KEY,
                RequestDeny.Operator.EXISTS, "key1");
        expectRequestDenyList.addRule(Collections.singletonList(headerKeyMatch));

        reportResponseBody = ((Config<Boolean>)builder.getConfig(ConfigKey.REPORT_RESPONSE_BODY)).get();
        Assert.assertFalse("REPORT_RESPONSE_BODY updated", reportResponseBody);
        reportMaxMethodPoolSize = ((Config<Integer>)builder.getConfig(ConfigKey.REPORT_MAX_METHOD_POOL_SIZE)).get();
        Assert.assertEquals("REPORT_MAX_METHOD_POOL_SIZE updated", 1000, reportMaxMethodPoolSize);
        requestDenyList = ((Config<RequestDenyList>)builder.getConfig(ConfigKey.REQUEST_DENY_LIST)).get();
        Assert.assertEquals("REQUEST_DENY_LIST updated", expectRequestDenyList, requestDenyList);

        // update invalid
        configString = "{\"gather_res_body\": \"invalid\"}";
        configJson = new JSONObject(configString);
        builder.update(configJson);
        configString = "{\"method_pool_max_length\": \"invalid\"}";
        configJson = new JSONObject(configString);
        builder.update(configJson);
        configString = "{\"blacklist_rules\": \"invalid\"}";
        configJson = new JSONObject(configString);
        builder.update(configJson);
        configString = "{\"invalid\": \"invalid\"}";
        configJson = new JSONObject(configString);
        builder.update(configJson);

        reportResponseBody = ((Config<Boolean>)builder.getConfig(ConfigKey.REPORT_RESPONSE_BODY)).get();
        Assert.assertFalse("REPORT_RESPONSE_BODY not updated", reportResponseBody);
        reportMaxMethodPoolSize = ((Config<Integer>)builder.getConfig(ConfigKey.REPORT_MAX_METHOD_POOL_SIZE)).get();
        Assert.assertEquals("REPORT_MAX_METHOD_POOL_SIZE not updated", 1000, reportMaxMethodPoolSize);
        requestDenyList = ((Config<RequestDenyList>)builder.getConfig(ConfigKey.REQUEST_DENY_LIST)).get();
        Assert.assertEquals("REQUEST_DENY_LIST not updated", expectRequestDenyList, requestDenyList);

        ConfigBuilder.clear();
    }
}