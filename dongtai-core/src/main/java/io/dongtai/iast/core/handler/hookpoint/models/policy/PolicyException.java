package io.dongtai.iast.core.handler.hookpoint.models.policy;

public class PolicyException extends Exception {
    public static final String ERR_POLICY_CONFIG_FROM_SERVER_INVALID = "policy config from server is invalid";
    public static final String ERR_POLICY_CONFIG_FILE_READ_FAILED = "read policy config file %s failed";
    public static final String ERR_POLICY_CONFIG_FILE_INVALID = "policy config file %s is invalid";
    public static final String ERR_POLICY_CONFIG_EMPTY = "policy config can not be empty";
    public static final String ERR_POLICY_NODE_EMPTY = "policy node can not be empty";
    public static final String ERR_POLICY_NODE_TYPE_INVALID = "policy node type is invalid";
    public static final String ERR_POLICY_NODE_INHERITABLE_INVALID = "policy node inheritable value is invalid";
    public static final String ERR_POLICY_NODE_SIGNATURE_INVALID = "policy node signature is invalid";
    public static final String ERR_POLICY_NODE_SOURCE_INVALID = "policy node source is invalid";
    public static final String ERR_POLICY_NODE_TARGET_INVALID = "policy node target is invalid";
    public static final String ERR_POLICY_SINK_NODE_VUL_TYPE_INVALID = "policy sink node vul type is invalid";
    public static final String ERR_POLICY_NODE_TAGS_UNTAGS_INVALID = "policy node tags/untags has invalid config";
    public static final String ERR_POLICY_NODE_RANGE_COMMAND_INVALID = "policy node range command is invalid";
    public static final String ERR_POLICY_NODE_STACK_BLACKLIST_INVALID = "policy node stack blacklist is invalid";

    public PolicyException(String message) {
        super(message);
    }

    public PolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
