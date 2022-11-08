package io.dongtai.iast.common.config;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestDeny {
    private static final String KEY_TARGET_TYPE = "target_type";
    private static final String KEY_OPERATOR = "operator";
    private static final String KEY_VALUE = "value";

    public enum TargetType {
        URL("URL"),
        HEADER_KEY("HEADER_KEY"),
        ;

        private String key;

        TargetType(String key) {
            this.key = key;
        }

        public static TargetType parse(String targetType) {
            for (TargetType t : values()) {
                if (t.key.equals(targetType)) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum Operator {
        EQUAL("EQUAL"),
        NOT_EQUAL("NOT_EQUAL"),
        CONTAIN("CONTAIN"),
        NOT_CONTAIN("NOT_CONTAIN"),
        ;

        private String key;

        Operator(String key) {
            this.key = key;
        }

        public static Operator parse(String operator) {
            for (Operator t : values()) {
                if (t.key.equals(operator)) {
                    return t;
                }
            }
            return null;
        }
    }

    private TargetType targetType;
    private Operator operator;
    private String value;

    public static RequestDeny parse(JSONObject config) {
        try {
            if (config == null) {
                return null;
            }

            TargetType targetType = TargetType.parse(config.getString(KEY_TARGET_TYPE));
            if (targetType == null) {
                return null;
            }
            Operator operator = Operator.parse(config.getString(KEY_OPERATOR));
            if (operator == null) {
                return null;
            }
            String value = config.getString(KEY_VALUE);
            if (value == null || value.isEmpty()) {
                return null;
            }

            RequestDeny requestDeny = new RequestDeny();
            requestDeny.targetType = targetType;
            requestDeny.operator = operator;
            requestDeny.value = value;
            return requestDeny;
        } catch (JSONException ignore) {
            return null;
        }
    }
}
