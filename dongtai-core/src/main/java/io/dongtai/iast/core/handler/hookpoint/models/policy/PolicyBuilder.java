package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.iast.core.utils.StringUtils;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.io.FileUtils;
import org.json.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class PolicyBuilder {
    private static final String KEY_DATA = "data";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_TARGET = "target";
    private static final String KEY_SIGNATURE = "signature";
    private static final String KEY_INHERIT = "inherit";
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
                int type = node.getInt(KEY_TYPE);
                PolicyNodeType nodeType = PolicyNodeType.get(type);
                if (nodeType == null) {
                    throw new PolicyException(PolicyException.ERR_POLICY_NODE_TYPE_INVALID + ": " + node.toString());
                }
                buildSource(policy, nodeType, node);
                buildPropagator(policy, nodeType, node);
                buildSink(policy, nodeType, node);
            } catch (JSONException e) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_TYPE_EMPTY + ": " + node.toString(), e);
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

        Set<TaintPosition> targets = null;
        try {
            targets = TaintPosition.parse(node.getString(KEY_TARGET));
            MethodMatcher methodMatcher = buildMethodMatcher(node);
            SourceNode sourceNode = new SourceNode(targets, methodMatcher);
            setInheritable(node, sourceNode);
            policy.addSource(sourceNode);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_SOURCE_NODE_INVALID + ": " + node.toString(), e);
        } catch (TaintPositionException e) {
            if (targets == null || targets.isEmpty()) {
                throw new PolicyException(PolicyException.ERR_POLICY_SOURCE_NODE_TARGET_INVALID + ": " + node.toString(), e);
            }
        }
    }

    public static void buildPropagator(Policy policy, PolicyNodeType type, JSONObject node) throws PolicyException {
        // @TODO: FILTER to tag list
        if (!PolicyNodeType.PROPAGATOR.equals(type) && !PolicyNodeType.FILTER.equals(type)) {
            return;
        }

        Set<TaintPosition> sources = null;
        Set<TaintPosition> targets = null;
        try {
            sources = TaintPosition.parse(node.getString(KEY_SOURCE));
            targets = TaintPosition.parse(node.getString(KEY_TARGET));
            MethodMatcher methodMatcher = buildMethodMatcher(node);
            // @TODO: command
            PropagatorNode propagatorNode = new PropagatorNode(sources, targets, null, new String[]{}, methodMatcher);
            setInheritable(node, propagatorNode);
            policy.addPropagator(propagatorNode);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_PROPAGATOR_NODE_INVALID + ": " + node.toString(), e);
        } catch (TaintPositionException e) {
            if (sources == null || sources.isEmpty()) {
                throw new PolicyException(PolicyException.ERR_POLICY_PROPAGATOR_NODE_SOURCE_INVALID + ": " + node.toString(), e);
            }
            if (targets == null || targets.isEmpty()) {
                throw new PolicyException(PolicyException.ERR_POLICY_PROPAGATOR_NODE_TARGET_INVALID + ": " + node.toString(), e);
            }
        }
    }

    public static void buildSink(Policy policy, PolicyNodeType type, JSONObject node) throws PolicyException {
        if (!PolicyNodeType.SINK.equals(type)) {
            return;
        }

        Set<TaintPosition> sources = null;
        try {
            sources = TaintPosition.parse(node.getString(KEY_SOURCE));
            MethodMatcher methodMatcher = buildMethodMatcher(node);
            SinkNode sinkNode = new SinkNode(sources, methodMatcher);
            setInheritable(node, sinkNode);
            policy.addSink(sinkNode);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_SINK_NODE_INVALID + ": " + node.toString(), e);
        } catch (TaintPositionException e) {
            if (sources == null || sources.isEmpty()) {
                throw new PolicyException(PolicyException.ERR_POLICY_SINK_NODE_SOURCE_INVALID + ": " + node.toString(), e);
            }
        }
    }

    private static MethodMatcher buildMethodMatcher(JSONObject node) throws PolicyException {
        try {
            String sign = node.getString(KEY_SIGNATURE);
            if (StringUtils.isEmpty(sign)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_EMPTY + ": " + node.toString());
            }
            Signature signature = Signature.parse(sign);

            // @TODO add other method matcher
            return new SignatureMethodMatcher(signature);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_NOT_EXISTS + ": " + node.toString(), e);
        } catch (IllegalArgumentException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID + ": " + node.toString(), e);
        }
    }

    private static void setInheritable(JSONObject node, PolicyNode policyNode) throws JSONException, PolicyException {
        Inheritable inheritable = Inheritable.parse(node.getString(KEY_INHERIT));
        policyNode.setInheritable(inheritable);
    }
}
