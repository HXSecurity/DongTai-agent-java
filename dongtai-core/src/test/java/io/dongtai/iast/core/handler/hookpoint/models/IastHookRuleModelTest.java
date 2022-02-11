package io.dongtai.iast.core.handler.hookpoint.models;

import io.dongtai.iast.core.utils.PropertyUtils;
import org.junit.Test;

public class IastHookRuleModelTest {
    @Test
    public void buildMoelFromServer() {
        PropertyUtils.getInstance(
                "～/workspace/secnium/BugPlatflam/dongtai/dongtai-agent-code/dongtai-agent/src/main/resources/iast.properties");
        IastHookRuleModel.buildModel();
    }
}