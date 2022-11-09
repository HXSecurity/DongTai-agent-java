package io.dongtai.iast.core.handler.hookpoint.models;

import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.TaintRanges;
import io.dongtai.iast.core.utils.StringUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.*;

/**
 * 方法事件
 *
 * @author dongzhiyong@huoxian.cn
 */
public class MethodEvent {
    /**
     * max display value size for object/return/parameters
     */
    private static final int MAX_VALUE_LENGTH = 1024;

    /**
     * method invoke id
     */
    private int invokeId;

    /**
     * is source policy node
     */
    public boolean source;

    private Set<TaintPosition> sourcePositions;
    private Set<TaintPosition> targetPositions;

    /**
     * current class name
     */
    private final String originClassName;

    /**
     * current event matched class name
     */
    private final String matchedClassName;

    /**
     * method name
     */
    private final String methodName;

    /**
     * method signature
     */
    public String signature;

    /**
     * method object instance
     */
    public Object objectInstance;

    /**
     * method object string value
     */
    public String objectValue;

    /**
     * method all parameters instances
     */
    public Object[] parameterInstances;

    /**
     * method all parameters string value
     */
    public List<Parameter> parameterValues = new ArrayList<Parameter>();

    /**
     * method return instance
     */
    public Object returnInstance;

    /**
     * method return string value
     */
    public String returnValue;

    private final Set<Integer> sourceHashes = new HashSet<Integer>();

    private final Set<Integer> targetHashes = new HashSet<Integer>();

    public List<MethodEventTargetRange> targetRanges = new ArrayList<MethodEventTargetRange>();

    public List<MethodEventSourceType> sourceTypes;

    private StackTraceElement callStack;

    public static class Parameter {
        private final String index;
        private final String value;

        public Parameter(String index, String value) {
            this.index = index;
            this.value = value;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("index", this.index);
            json.put("value", this.value);
            return json;
        }
    }

    public static class MethodEventSourceType {
        private final Integer hash;
        private final String type;

        public MethodEventSourceType(Integer hash, String type) {
            this.hash = hash;
            this.type = type;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("hash", this.hash);
            json.put("type", this.type);
            return json;
        }
    }

    public static class MethodEventTargetRange {
        private final Integer hash;
        private final TaintRanges ranges;

        public MethodEventTargetRange(Integer hash, TaintRanges ranges) {
            this.hash = hash;
            this.ranges = ranges;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("hash", this.hash);
            json.put("ranges", this.ranges.toJson());
            return json;
        }
    }

    public MethodEvent(final String originClassName, final String matchedClassName,
                       final String methodName, final String signature,
                       final Object objectInstance, final Object[] parameterInstances, Object returnInstance) {
        this.matchedClassName = matchedClassName;
        this.originClassName = originClassName;
        this.methodName = methodName;
        this.signature = signature;
        this.objectInstance = objectInstance;
        this.parameterInstances = parameterInstances;
        this.returnInstance = returnInstance;
        this.source = false;
    }

    public int getInvokeId() {
        return invokeId;
    }

    public void setInvokeId(int invokeId) {
        this.invokeId = invokeId;
    }

    public boolean isSource() {
        return source;
    }

    public void setTaintPositions(Set<TaintPosition> sourcePositions, Set<TaintPosition> targetPositions) {
        this.sourcePositions = sourcePositions;
        this.targetPositions = targetPositions;
    }

    public Set<TaintPosition> getSourcePositions() {
        return this.sourcePositions;
    }

    public Set<TaintPosition> getTargetPositions() {
        return this.targetPositions;
    }

    public String getOriginClassName() {
        return originClassName;
    }

    public String getMatchedClassName() {
        return matchedClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setObjectValue(Object obj, boolean hasTaint) {
        if (obj == null) {
            return;
        }
        this.objectValue = formatValue(obj, hasTaint);
    }

    public void addParameterValue(int index, Object param, boolean hasTaint) {
        if (param == null) {
            return;
        }
        String indexString = "P" + String.valueOf(index + 1);
        Parameter parameter = new Parameter(indexString, formatValue(param, hasTaint));
        this.parameterValues.add(parameter);
    }

    public void setReturnValue(Object ret, boolean hasTaint) {
        if (ret == null) {
            return;
        }
        this.returnValue = formatValue(ret, hasTaint);
    }

    private String formatValue(Object val, boolean hasTaint) {
        String str = obj2String(val);
        return "[" + StringUtils.normalize(str, MAX_VALUE_LENGTH) + "]"
                + (hasTaint ? "*" : "") + String.valueOf(str.length());
    }

    public Set<Integer> getSourceHashes() {
        return sourceHashes;
    }

    public void addSourceHash(int hashcode) {
        this.sourceHashes.add(hashcode);
    }

    public Set<Integer> getTargetHashes() {
        return targetHashes;
    }

    public void addTargetHash(int hashCode) {
        this.targetHashes.add(hashCode);
    }

    public String getCallerClass() {
        return callStack.getClassName();
    }

    public String getCallerMethod() {
        return callStack.getMethodName();
    }

    public int getCallerLine() {
        return callStack.getLineNumber();
    }

    public void setCallStacks(StackTraceElement[] callStacks) {
        this.setCallStack(callStacks[1]);
    }

    public void setCallStack(StackTraceElement callStack) {
        this.callStack = callStack;
    }

    public String obj2String(Object value) {
        StringBuilder sb = new StringBuilder();
        if (null == value) {
            return "";
        }
        try {
            if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
                // 判断是否是基本类型的数组，基本类型的数组无法类型转换为Object[]，导致java.lang.ClassCastException异常
                Object[] taints = (Object[]) value;
                for (Object taint : taints) {
                    if (taint != null) {
                        if (taint.getClass().isArray() && !taint.getClass().getComponentType().isPrimitive()) {
                            Object[] subTaints = (Object[]) taint;
                            for (Object subTaint : subTaints) {
                                sb.append(subTaint.toString()).append(" ");
                            }
                        } else {
                            sb.append(taint.toString()).append(" ");
                        }
                    }
                }
            } else if (value instanceof StringWriter) {
                sb.append(((StringWriter) value).getBuffer().toString());
            } else {
                sb.append(value.toString());
            }
        } catch (Exception e) {
            DongTaiLog.warn("convert object " + value.toString() + " to string failed", e);
        }
        return sb.toString();
    }
}
