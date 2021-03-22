package com.secnium.iast.core.handler.models;

import com.secnium.iast.core.PropertyUtils;
import org.junit.Test;

public class IASTHookRuleModelTest {
    @Test
    public void buildMoelFromServer() {
        PropertyUtils.getInstance("～/workspace/secnium/BugPlatflam/lingzhi/lingzhi-agent-code/iast-agent/src/main/resources/iast.properties");
        IASTHookRuleModel.buildModelRemote();
    }
}