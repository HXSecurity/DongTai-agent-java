package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.utils.StringUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONArray;

public class PolicyManager {
    private Policy policy;

    public Policy getPolicy() {
        return this.policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public void loadPolicy(String policyPath) {
        try {
            JSONArray policyConfig;
            if (StringUtils.isEmpty(policyPath)) {
                policyConfig = PolicyBuilder.fetchFromServer();
            } else {
                policyConfig = PolicyBuilder.fetchFromFile(policyPath);
            }
            this.policy = PolicyBuilder.build(policyConfig);
        } catch (Throwable e) {
            DongTaiLog.error("load policy failed", e);
        }
    }
}
