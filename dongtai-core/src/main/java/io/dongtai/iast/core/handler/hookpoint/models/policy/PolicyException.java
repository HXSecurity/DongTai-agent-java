package io.dongtai.iast.core.handler.hookpoint.models.policy;

public class PolicyException extends Exception {
    public static final String ERR_POLICY_CONFIG_FROM_SERVER_INVALID = "policy config from server is invalid";
    public static final String ERR_POLICY_CONFIG_FILE_READ_FAILED = "read policy config file %s failed";
    public static final String ERR_POLICY_CONFIG_FILE_INVALID = "policy config file %s is invalid";
    public static final String ERR_POLICY_CONFIG_EMPTY = "policy config can not be empty";
    public static final String ERR_POLICY_NODE_EMPTY = "policy node can not be empty";
    public static final String ERR_POLICY_NODE_TYPE_EMPTY = "policy node type cat not be empty";
    public static final String ERR_POLICY_NODE_TYPE_INVALID = "policy node type is invalid";
    public static final String ERR_POLICY_NODE_INHERITABLE_INVALID = "policy node inheritable value is invalid";
    public static final String ERR_POLICY_NODE_SIGNATURE_NOT_EXISTS = "policy node signature does not exists";
    public static final String ERR_POLICY_NODE_SIGNATURE_EMPTY = "policy node signature cat not be empty";
    public static final String ERR_POLICY_NODE_SIGNATURE_INVALID = "policy node signature is invalid";
    public static final String ERR_POLICY_SOURCE_NODE_INVALID = "policy source node is invalid";
    public static final String ERR_POLICY_SOURCE_NODE_TARGET_INVALID = "policy source node target is invalid";
    public static final String ERR_POLICY_PROPAGATOR_NODE_INVALID = "policy propagator node is invalid";
    public static final String ERR_POLICY_PROPAGATOR_NODE_SOURCE_INVALID = "policy propagator node source is invalid";
    public static final String ERR_POLICY_PROPAGATOR_NODE_TARGET_INVALID = "policy propagator node target is invalid";
    public static final String ERR_POLICY_SINK_NODE_INVALID = "policy sink node is invalid";
    public static final String ERR_POLICY_SINK_NODE_SOURCE_INVALID = "policy sink node source is invalid";

    public PolicyException(String message) {
        super(message);
    }

    public PolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
