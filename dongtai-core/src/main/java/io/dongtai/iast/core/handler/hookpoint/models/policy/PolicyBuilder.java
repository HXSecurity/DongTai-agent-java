package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.core.handler.hookpoint.vulscan.VulnType;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.iast.core.utils.StringUtils;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.io.FileUtils;
import org.json.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PolicyBuilder {
    private static final String KEY_DATA = "data";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_TARGET = "target";
    private static final String KEY_SIGNATURE = "signature";
    private static final String KEY_INHERIT = "inherit";
    private static final String KEY_VUL_TYPE = "vul_type";
    private static final String KEY_COMMAND = "command";

    public static JSONArray fetchFromServer() throws PolicyException {
        try {
            StringBuilder resp = HttpClientUtils.sendGet(ApiPath.HOOK_PROFILE, null);
            JSONObject respObj = new JSONObject(resp.toString());
            return respObj.getJSONArray(KEY_DATA);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_CONFIG_FROM_SERVER_INVALID, e);
        }
    }

    public static JSONArray fetchFromFile(String path) throws PolicyException {
        try {
            File file = new File(path);
            String content = FileUtils.readFileToString(file);
            JSONObject respObj = new JSONObject(content);
            return respObj.getJSONArray(KEY_DATA);
        } catch (IOException e) {
            throw new PolicyException(String.format(PolicyException.ERR_POLICY_CONFIG_FILE_READ_FAILED, path), e);
        } catch (JSONException e) {
            throw new PolicyException(String.format(PolicyException.ERR_POLICY_CONFIG_FILE_INVALID, path), e);
        }
    }

    public static Policy build(JSONArray policyConfig) throws PolicyException {
        if (policyConfig == null || policyConfig.length() == 0) {
            throw new PolicyException(PolicyException.ERR_POLICY_CONFIG_EMPTY);
        }
        int policyLen = policyConfig.length();
        Policy policy = new Policy();
        for (int i = 0; i < policyLen; i++) {
            JSONObject node = policyConfig.getJSONObject(i);
            if (node == null || node.length() == 0) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_EMPTY);
            }

            try {
                PolicyNodeType nodeType = parseNodeType(node);
                buildSource(policy, nodeType, node);
                buildPropagator(policy, nodeType, node);
                buildSink(policy, nodeType, node);
            } catch (PolicyException e) {
                DongTaiLog.error(e.getMessage());
            }
        }
        return policy;
    }

    public static void buildSource(Policy policy, PolicyNodeType type, JSONObject node) throws PolicyException {
        if (!PolicyNodeType.SOURCE.equals(type)) {
            return;
        }

        Set<TaintPosition> sources = parseSource(node, type);
        Set<TaintPosition> targets = parseTarget(node, type);
        MethodMatcher methodMatcher = buildMethodMatcher(node);
        SourceNode sourceNode = new SourceNode(sources, targets, methodMatcher);
        setInheritable(node, sourceNode);
        policy.addSource(sourceNode);
    }

    public static void buildPropagator(Policy policy, PolicyNodeType type, JSONObject node) throws PolicyException {
        // @TODO: FILTER to tag list
        if (!PolicyNodeType.PROPAGATOR.equals(type) && !PolicyNodeType.FILTER.equals(type)) {
            return;
        }

        Set<TaintPosition> sources = parseSource(node, type);
        Set<TaintPosition> targets = parseTarget(node, type);
        MethodMatcher methodMatcher = buildMethodMatcher(node);
        // @TODO: command
        PropagatorNode propagatorNode = new PropagatorNode(sources, targets, null, new String[]{}, methodMatcher);
        setInheritable(node, propagatorNode);
        policy.addPropagator(propagatorNode);
    }

    public static void buildSink(Policy policy, PolicyNodeType type, JSONObject node) throws PolicyException {
        if (!PolicyNodeType.SINK.equals(type)) {
            return;
        }

        MethodMatcher methodMatcher = buildMethodMatcher(node);
        String vulType = parseVulType(node);
        SinkNode sinkNode;
        if (VulnType.CRYPTO_WEAK_RANDOMNESS.equals(vulType)) {
            sinkNode = new SinkNode(new HashSet<TaintPosition>(), methodMatcher);
        } else {
            sinkNode = new SinkNode(parseSource(node, type), methodMatcher);
        }
        setInheritable(node, sinkNode);
        sinkNode.setVulType(vulType);
        policy.addSink(sinkNode);
    }

    private static PolicyNodeType parseNodeType(JSONObject node) throws PolicyException {
        try {
            int type = node.getInt(KEY_TYPE);
            PolicyNodeType nodeType = PolicyNodeType.get(type);
            if (nodeType == null) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_TYPE_INVALID + ": " + node.toString());
            }
            return nodeType;
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_TYPE_INVALID + ": " + node.toString(), e);
        }
    }

    private static Set<TaintPosition> parseSource(JSONObject node, PolicyNodeType type) throws PolicyException {
        try {
            return TaintPosition.parse(node.getString(KEY_SOURCE));
        } catch (JSONException e) {
            if (!PolicyNodeType.SOURCE.equals(type) && !PolicyNodeType.FILTER.equals(type)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_SOURCE_INVALID + ": " + node.toString(), e);
            }
        } catch (TaintPositionException e) {
            if (!PolicyNodeType.SOURCE.equals(type) && !PolicyNodeType.FILTER.equals(type)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_SOURCE_INVALID + ": " + node.toString(), e);
            }
        }
        return new HashSet<TaintPosition>();
    }

    private static Set<TaintPosition> parseTarget(JSONObject node, PolicyNodeType type) throws PolicyException {
        try {
            return TaintPosition.parse(node.getString(KEY_TARGET));
        } catch (JSONException e) {
            if (!PolicyNodeType.FILTER.equals(type)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_TARGET_INVALID + ": " + node.toString(), e);
            }
        } catch (TaintPositionException e) {
            if (!PolicyNodeType.FILTER.equals(type)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_TARGET_INVALID + ": " + node.toString(), e);
            }
        }
        return new HashSet<TaintPosition>();
    }

    private static void setInheritable(JSONObject node, PolicyNode policyNode) throws PolicyException {
        try {
            Inheritable inheritable = Inheritable.parse(node.getString(KEY_INHERIT));
            policyNode.setInheritable(inheritable);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_INHERITABLE_INVALID + ": " + node.toString(), e);
        }
    }

    private static String parseVulType(JSONObject node) throws PolicyException {
        try {
            String vulType = node.getString(KEY_VUL_TYPE);
            if (vulType == null || vulType.isEmpty()) {
                throw new PolicyException(PolicyException.ERR_POLICY_SINK_NODE_VUL_TYPE_INVALID + ": " + node.toString());
            }
            return vulType;
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_SINK_NODE_VUL_TYPE_INVALID + ": " + node.toString(), e);
        }
    }

    private static MethodMatcher buildMethodMatcher(JSONObject node) throws PolicyException {
        try {
            String sign = node.getString(KEY_SIGNATURE);
            if (StringUtils.isEmpty(sign)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID + ": " + node.toString());
            }
            Signature signature = Signature.parse(sign);

            // @TODO add other method matcher
            return new SignatureMethodMatcher(signature);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID + ": " + node.toString(), e);
        } catch (IllegalArgumentException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID + ": " + node.toString(), e);
        }
    }
}
