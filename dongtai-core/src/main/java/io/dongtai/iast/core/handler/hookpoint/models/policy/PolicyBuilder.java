package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintCommand;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintCommandRunner;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;
import io.dongtai.iast.core.handler.hookpoint.vulscan.VulnType;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.iast.common.string.StringUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PolicyBuilder {
    private static final String KEY_DATA = "data";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_TARGET = "target";
    private static final String KEY_SIGNATURE = "signature";
    private static final String KEY_INHERIT = "inherit";
    private static final String KEY_VUL_TYPE = "vul_type";
    private static final String KEY_TAGS = "tags";
    private static final String KEY_UNTAGS = "untags";
    private static final String KEY_COMMAND = "command";
    private static final String KEY_STACK_BLACKLIST = "stack_blacklist";
    private static final String KEY_IGNORE_INTERNAL = "ignore_internal";
    private static final String KEY_IGNORE_BLACKLIST = "ignore_blacklist";

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
                buildValidator(policy, nodeType, node);
            } catch (PolicyException e) {
                DongTaiLog.warn(ErrorCode.get("POLICY_CONFIG_INVALID"), e);
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
        List<String[]> tags = parseTags(node, sourceNode);
        sourceNode.setTags(tags.get(0));
        parseFlags(node, sourceNode);
        policy.addSource(sourceNode);
    }

    public static void buildPropagator(Policy policy, PolicyNodeType type, JSONObject node) throws PolicyException {
        if (!PolicyNodeType.PROPAGATOR.equals(type)) {
            return;
        }

        Set<TaintPosition> sources = parseSource(node, type);
        Set<TaintPosition> targets = parseTarget(node, type);
        MethodMatcher methodMatcher = buildMethodMatcher(node);
        PropagatorNode propagatorNode = new PropagatorNode(sources, targets, methodMatcher);
        setInheritable(node, propagatorNode);
        List<String[]> tags = parseTags(node, propagatorNode);
        propagatorNode.setTags(tags.get(0));
        propagatorNode.setUntags(tags.get(1));
        parseCommand(node, propagatorNode);
        parseFlags(node, propagatorNode);
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
        parseStackDenyList(node, sinkNode);
        parseFlags(node, sinkNode);
        policy.addSink(sinkNode);
    }

    public static void buildValidator(Policy policy, PolicyNodeType type, JSONObject node) throws PolicyException {
        if (!PolicyNodeType.VALIDATOR.equals(type)) {
            return;
        }

        Set<TaintPosition> sources = parseSource(node, type);
        MethodMatcher methodMatcher = buildMethodMatcher(node);
        ValidatorNode validatorNode = new ValidatorNode(sources, methodMatcher);
        setInheritable(node, validatorNode);
        List<String[]> tags = parseTags(node, validatorNode);
        validatorNode.setTags(tags.get(0));
        parseFlags(node, validatorNode);
        policy.addValidator(validatorNode);
    }

    private static PolicyNodeType parseNodeType(JSONObject node) throws PolicyException {
        try {
            int type = node.getInt(KEY_TYPE);
            PolicyNodeType nodeType = PolicyNodeType.get(type);
            if (nodeType == null) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_TYPE_INVALID + ": " + node);
            }
            return nodeType;
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_TYPE_INVALID + ": " + node, e);
        }
    }

    private static Set<TaintPosition> parseSource(JSONObject node, PolicyNodeType type) throws PolicyException {
        try {
            return TaintPosition.parse(node.getString(KEY_SOURCE));
        } catch (JSONException e) {
            if (!PolicyNodeType.SOURCE.equals(type)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_SOURCE_INVALID + ": " + node, e);
            }
        } catch (TaintPositionException e) {
            if (!PolicyNodeType.SOURCE.equals(type)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_SOURCE_INVALID + ": " + node, e);
            }
        }
        return new HashSet<TaintPosition>();
    }

    private static Set<TaintPosition> parseTarget(JSONObject node, PolicyNodeType type) throws PolicyException {
        try {
            return TaintPosition.parse(node.getString(KEY_TARGET));
        } catch (JSONException e) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_TARGET_INVALID + ": " + node, e);
        } catch (TaintPositionException e) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_TARGET_INVALID + ": " + node, e);
        }
    }

    private static void setInheritable(JSONObject node, PolicyNode policyNode) throws PolicyException {
        try {
            Inheritable inheritable = Inheritable.parse(node.getString(KEY_INHERIT));
            policyNode.setInheritable(inheritable);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_INHERITABLE_INVALID + ": " + node, e);
        }
    }

    private static String parseVulType(JSONObject node) throws PolicyException {
        try {
            String vulType = node.getString(KEY_VUL_TYPE);
            if (vulType == null || vulType.isEmpty()) {
                throw new PolicyException(PolicyException.ERR_POLICY_SINK_NODE_VUL_TYPE_INVALID + ": " + node);
            }
            return vulType;
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_SINK_NODE_VUL_TYPE_INVALID + ": " + node, e);
        }
    }

    private static MethodMatcher buildMethodMatcher(JSONObject node) throws PolicyException {
        try {
            String sign = node.getString(KEY_SIGNATURE);
            if (StringUtils.isEmpty(sign)) {
                throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID + ": " + node);
            }
            Signature signature = Signature.parse(sign);

            // @TODO add other method matcher
            return new SignatureMethodMatcher(signature);
        } catch (JSONException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID + ": " + node, e);
        } catch (IllegalArgumentException e) {
            throw new PolicyException(PolicyException.ERR_POLICY_NODE_SIGNATURE_INVALID + ": " + node, e);
        }
    }

    /**
     * stack deny list for sink node
     */
    private static void parseStackDenyList(JSONObject node, SinkNode sinkNode) {
        try {
            if (node.has(KEY_STACK_BLACKLIST)) {
                JSONArray arr = node.getJSONArray(KEY_STACK_BLACKLIST);
                sinkNode.setStackDenyList(arr.toList().toArray(new String[0]));
            }
        } catch (JSONException ignore) {
            DongTaiLog.warn(ErrorCode.get("POLICY_CONFIG_INVALID"),
                    new PolicyException(PolicyException.ERR_POLICY_NODE_STACK_BLACKLIST_INVALID + ": " + node));
        } catch (ArrayStoreException ignore) {
            DongTaiLog.warn(ErrorCode.get("POLICY_CONFIG_INVALID"),
                    new PolicyException(PolicyException.ERR_POLICY_NODE_STACK_BLACKLIST_INVALID + ": " + node));
        }
    }

    private static List<String[]> parseTags(JSONObject node, PolicyNode policyNode) {
        List<String[]> empty = Arrays.asList(new String[0], new String[0]);
        if (!(policyNode.getMethodMatcher() instanceof SignatureMethodMatcher)) {
            return empty;
        }

        boolean hasInvalid = false;
        List<String> tags = new ArrayList<String>();
        List<String> untags = new ArrayList<String>();
        try {
            if (node.has(KEY_TAGS)) {
                JSONArray ts = node.getJSONArray(KEY_TAGS);
                for (Object o : ts) {
                    String t = (String) o;
                    if (TaintTag.UNTRUSTED.equals(t)) {
                        continue;
                    }
                    if (TaintTag.get(t) != null) {
                        tags.add(TaintTag.get(t).getKey());
                    } else {
                        hasInvalid = true;
                    }
                }
            }
        } catch (JSONException ignore) {
            hasInvalid = true;
        } catch (ClassCastException ignore) {
            hasInvalid = true;
        }

        try {
            if (node.has(KEY_UNTAGS)) {
                JSONArray uts = node.getJSONArray(KEY_UNTAGS);
                for (Object o : uts) {
                    String ut = (String) o;
                    if (TaintTag.UNTRUSTED.equals(ut)) {
                        continue;
                    }
                    TaintTag tt = TaintTag.get(ut);
                    if (tt != null) {
                        if (tags.contains(tt.getKey())) {
                            hasInvalid = true;
                        }
                        untags.add(tt.getKey());
                    } else {
                        hasInvalid = true;
                    }
                }
            }
        } catch (JSONException ignore) {
            hasInvalid = true;
        } catch (ClassCastException ignore) {
            hasInvalid = true;
        }

        if (hasInvalid) {
            DongTaiLog.warn(ErrorCode.get("POLICY_CONFIG_INVALID"),
                    new PolicyException(PolicyException.ERR_POLICY_NODE_TAGS_UNTAGS_INVALID + ": " + node));
        }

        return Arrays.asList(tags.toArray(new String[0]), untags.toArray(new String[0]));
    }

    private static void parseCommand(JSONObject node, PropagatorNode propagatorNode) {
        try {
            if (node.has(KEY_COMMAND)) {
                String cmdConfig = node.getString(KEY_COMMAND);
                if (cmdConfig == null) {
                    return;
                }
                cmdConfig = cmdConfig.trim();
                if (cmdConfig.isEmpty()) {
                    return;
                }

                boolean isInvalid = false;
                int parametersStartIndex = cmdConfig.indexOf("(");
                int parametersEndIndex = cmdConfig.indexOf(")");

                if (parametersStartIndex <= 0 || parametersEndIndex <= 1
                        || parametersStartIndex > parametersEndIndex
                        || parametersEndIndex != cmdConfig.length() - 1) {
                    isInvalid = true;
                } else {
                    String cmd = cmdConfig.substring(0, parametersStartIndex).trim();
                    String argumentsStr = cmdConfig.substring(parametersStartIndex + 1, parametersEndIndex).trim();
                    String[] arguments = new String[]{};
                    if (!argumentsStr.isEmpty()) {
                        argumentsStr = argumentsStr.toUpperCase();
                        arguments = argumentsStr.replace(" ", "").split(",");
                        for (String argument : arguments) {
                            String dig = argument;
                            if (dig.startsWith("P")) {
                                dig = dig.substring(1);
                            }
                            if (!dig.matches("\\d+")) {
                                isInvalid = true;
                                break;
                            }
                        }
                    }

                    TaintCommand command = TaintCommand.get(cmd);
                    if (command == null) {
                        isInvalid = true;
                    } else {
                        if (!(propagatorNode.getMethodMatcher() instanceof SignatureMethodMatcher)) {
                            return;
                        }
                        String signature = ((SignatureMethodMatcher) propagatorNode.getMethodMatcher()).getSignature().toString();
                        TaintCommandRunner commandRunner = TaintCommandRunner.create(signature, command, arguments);
                        propagatorNode.setCommandRunner(commandRunner);
                    }
                }

                if (isInvalid) {
                    DongTaiLog.warn(ErrorCode.get("POLICY_CONFIG_INVALID"),
                            new PolicyException(PolicyException.ERR_POLICY_NODE_RANGE_COMMAND_INVALID + ": " + node));
                }
            }
        } catch (JSONException ignore) {
            DongTaiLog.warn(ErrorCode.get("POLICY_CONFIG_INVALID"),
                    new PolicyException(PolicyException.ERR_POLICY_NODE_RANGE_COMMAND_INVALID + ": " + node));
        }
    }

    private static void parseFlags(JSONObject node, PolicyNode policyNode) {
        try {
            boolean ignoreInternal = node.getBoolean(KEY_IGNORE_INTERNAL);
            policyNode.setIgnoreInternal(ignoreInternal);
        } catch (JSONException ignore) {
        }

        try {
            boolean ignoreBlackList = node.getBoolean(KEY_IGNORE_BLACKLIST);
            policyNode.setIgnoreBlacklist(ignoreBlackList);
        } catch (JSONException ignore) {
        }
    }
}
