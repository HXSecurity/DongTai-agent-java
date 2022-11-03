package io.dongtai.iast.core.handler.hookpoint.models.policy;

public class Signature {
    public static final String ERR_SIGNATURE_EMPTY = "signature can not be empty";
    public static final String ERR_SIGNATURE_INVALID = "signature is invalid";

    private String signature;
    private String className;
    private String methodName;
    private String[] parameters;

    public Signature(String className, String methodName, String[] parameters) {
        this.className = className;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameters() {
        return this.parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public static Signature parse(String sign) {
        if (sign == null) {
            throw new IllegalArgumentException(ERR_SIGNATURE_EMPTY);
        }
        sign = sign.replaceAll(" ", "");
        if (sign.isEmpty()) {
            throw new IllegalArgumentException(ERR_SIGNATURE_EMPTY);
        }
        int parametersStartIndex = sign.indexOf("(");
        int parametersEndIndex = sign.indexOf(")");
        // a.b()
        if (parametersStartIndex <= 2 || parametersEndIndex <= 3
                || parametersStartIndex > parametersEndIndex
                || parametersEndIndex != sign.length() -1) {
            throw new IllegalArgumentException(ERR_SIGNATURE_INVALID + ": " + sign);
        }

        String classAndMethod = sign.substring(0, parametersStartIndex);
        int methodStartIndex = classAndMethod.lastIndexOf(".");
        if (methodStartIndex <= 0) {
            throw new IllegalArgumentException(ERR_SIGNATURE_INVALID + ": " + sign);
        }

        String className = classAndMethod.substring(0, methodStartIndex);
        String methodName = classAndMethod.substring(methodStartIndex + 1, parametersStartIndex);
        String parametersStr = sign.substring(parametersStartIndex + 1, parametersEndIndex).trim();
        if (!parametersStr.isEmpty() && (parametersStr.contains("(") || parametersStr.contains(")"))) {
            throw new IllegalArgumentException(ERR_SIGNATURE_INVALID + ": " + sign);
        }

        String[] parameters = new String[]{};
        if (!parametersStr.isEmpty()) {
            parameters = parametersStr.split(",");
        }
        return new Signature(className, methodName, parameters);
    }

    public static String normalizeSignature(String className, String methodName, String[] parameters) {
        StringBuilder sb = new StringBuilder(64);
        sb.append(className);
        sb.append('.');
        sb.append(methodName);
        sb.append('(');
        if (parameters != null && parameters.length != 0) {
            int i = 0;
            for (String parameter : parameters) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(parameter);
                i++;
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public void updateSignature() {
        this.signature = normalizeSignature(this.className, this.methodName, this.parameters);
    }

    @Override
    public String toString() {
        if (this.signature == null) {
            updateSignature();
        }
        return this.signature;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Signature)) {
            return false;
        }

        return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
