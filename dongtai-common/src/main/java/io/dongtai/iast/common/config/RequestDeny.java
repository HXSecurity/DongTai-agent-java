package io.dongtai.iast.common.config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class RequestDeny {
    private static final String KEY_TARGET_TYPE = "target_type";
    private static final String KEY_OPERATOR = "operator";
    private static final String KEY_VALUE = "value";

    private static final Map<TargetType, List<Operator>> OPERATOR_MAP = new HashMap<TargetType, List<Operator>>() {{
        put(TargetType.URL, Arrays.asList(Operator.EQUAL, Operator.NOT_EQUAL, Operator.CONTAIN, Operator.NOT_CONTAIN));
        put(TargetType.HEADER_KEY, Arrays.asList(Operator.EXISTS, Operator.NOT_EXISTS));
    }};

    public enum TargetType {
        URL("URL"),
        HEADER_KEY("HEADER_KEY"),
        ;

        private final String key;

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

        public String getKey() {
            return this.key;
        }
    }

    public enum Operator {
        EQUAL("EQUAL"),
        NOT_EQUAL("NOT_EQUAL"),
        CONTAIN("CONTAIN"),
        NOT_CONTAIN("NOT_CONTAIN"),
        EXISTS("EXISTS"),
        NOT_EXISTS("NOT_EXISTS"),
        ;

        private final String key;

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

        public String getKey() {
            return this.key;
        }
    }

    private final TargetType targetType;
    private final Operator operator;
    private final String value;

    public RequestDeny(TargetType targetType, Operator operator, String value) {
        this.targetType = targetType;
        this.operator = operator;
        this.value = value;
    }

    public static RequestDeny parse(JSONObject config) {
        try {
            if (config == null) {
                return null;
            }

            TargetType targetType = TargetType.parse(config.getString(KEY_TARGET_TYPE));
            if (targetType == null || OPERATOR_MAP.get(targetType) == null) {
                return null;
            }
            Operator operator = Operator.parse(config.getString(KEY_OPERATOR));
            if (operator == null || !OPERATOR_MAP.get(targetType).contains(operator)) {
                return null;
            }

            String value = config.getString(KEY_VALUE);
            if (value == null || value.isEmpty()) {
                return null;
            }

            return new RequestDeny(targetType, operator, value);
        } catch (JSONException ignore) {
            return null;
        }
    }

    public boolean match(String url, Map<String, String> headers) {
        if (TargetType.URL.equals(this.targetType)) {
            return matchUrl(url);
        } else if (TargetType.HEADER_KEY.equals(this.targetType)) {
            return matchHeaderKey(headers);
        }

        return false;
    }

    private boolean matchUrl(String url) {
        if (url == null || url.isEmpty()) {
            // skip empty url
            return true;
        }

        String uri;
        if (url.contains("?")) {
            uri = url.substring(0, url.indexOf("?", 1));
        } else {
            uri = url;
        }

        return matchOperator(uri);
    }

    private boolean matchHeaderKey(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return false;
        }

        boolean exists = false;
        String matchVal = this.value.toLowerCase();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (matchVal.equals(entry.getKey().toLowerCase())) {
                exists = true;
                break;
            }
        }

        if (Operator.EXISTS.equals(operator)) {
            return exists;
        } else if (Operator.NOT_EXISTS.equals(operator)) {
            return !exists;
        }

        return false;
    }

    private boolean matchOperator(String val) {
        val = val.toLowerCase();
        String matchVal = this.value.toLowerCase();
        if (Operator.EQUAL.equals(this.operator)) {
            return matchVal.equals(val);
        } else if (Operator.NOT_EQUAL.equals(this.operator)) {
            return !matchVal.equals(val);
        } else if (Operator.CONTAIN.equals(this.operator)) {
            return val.contains(matchVal);
        } else if (Operator.NOT_CONTAIN.equals(this.operator)) {
            return !val.contains(matchVal);
        }

        return false;
    }

    @Override
    public String toString() {
        return targetType.getKey() + "/" + operator.getKey() + "/" + value;
    }
}
