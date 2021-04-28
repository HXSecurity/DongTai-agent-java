package com.secnium.iast.core.handler.models;

import com.secnium.iast.core.PropertyUtils;
import org.junit.Test;

public class IastHookRuleModelTest {
    @Test
    public void buildMoelFromServer() {
        PropertyUtils.getInstance(
                "～/workspace/secnium/BugPlatflam/dongtai/dongtai-agent-code/iast-agent/src/main/resources/iast.properties");
        IastHookRuleModel.buildModel();
    }
}