package io.dongtai.iast.core.handler.hookpoint.models;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.string.ObjectFormatResult;
import io.dongtai.iast.common.string.ObjectFormatter;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;
import io.dongtai.iast.core.utils.PropertyUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 方法事件
 *
 * @author dongzhiyong@huoxian.cn
 */
public class MethodEvent {

    /**
     * method invoke id
     */
    private int invokeId;

    /**
     * policy type
     */
    private String policyType;

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
    public List<Parameter> parameterValues = new ArrayList<>();

    /**
     * method return instance
     */
    public Object returnInstance;

    /**
     * method return string value
     */
    public String returnValue;

    private final Set<Long> sourceHashes = new HashSet<>();

    private final Set<Long> targetHashes = new HashSet<>();

    public List<MethodEventTargetRange> targetRanges = new ArrayList<>();

    public List<MethodEventTargetRange> sourceRanges = new ArrayList<>();

    public List<MethodEventSourceType> sourceTypes;

    private StackTraceElement callStack;

    private List<Object> stacks;

    public String traceId = null;

    public static class Parameter {
        private final String index;
        private final String value;

        public Parameter(String index, String value) {
            this.index = index;
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("index", this.index);
            json.put("value", this.value);
            return json;
        }
    }

    public static class MethodEventSourceType {
        private final Long hash;
        private final String type;

        public MethodEventSourceType(Long hash, String type) {
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
        private final Long hash;
        private final TaintRanges ranges;

        public MethodEventTargetRange(Long hash, TaintRanges ranges) {
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

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
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
        String indexString = "P" + (index + 1);
        Parameter parameter = new Parameter(indexString, formatValue(param, hasTaint));
        this.parameterValues.add(parameter);
    }

    public void setReturnValue(Object ret, boolean hasTaint) {
        if (ret == null) {
            return;
        }
        this.returnValue = formatValue(ret, hasTaint);
    }

    private static String formatValue(Object val, boolean hasTaint) {
        ObjectFormatResult r = formatObject(val);
        return "[" + r.objectFormatString + "]"
                + (hasTaint ? "*" : "") + r.originalLength;
    }

    public Set<Long> getSourceHashes() {
        return sourceHashes;
    }

    public void addSourceHash(long hashcode) {
        this.sourceHashes.add(hashcode);
    }

    public Set<Long> getTargetHashes() {
        return targetHashes;
    }

    public void addTargetHash(long hashCode) {
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

    /**
     * @param value
     * @return
     * @deprecated 开始是发现了有性能问题，然后进行了一版修改，这版修改影响到了与服务端的数据结构交互逻辑，因此废弃，再重写一版
     */
    @Deprecated()
    public static String obj2String(Object value) {
        int taintValueLength = PropertyUtils.getTaintToStringCharLimit();
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
                                appendWithMaxLength(sb, subTaint.toString(), taintValueLength);
                                sb.append(" ");
                            }
                        } else {
                            appendWithMaxLength(sb, taint.toString(), taintValueLength);
                            sb.append(" ");
                        }
                    }
                }
            } else if (value instanceof StringWriter) {
                appendWithMaxLength(sb, ((StringWriter) value).getBuffer().toString(), taintValueLength);
            } else {
                appendWithMaxLength(sb, value.toString(), taintValueLength);
            }
        } catch (Throwable e) {
            // org.jruby.RubyBasicObject.hashCode() may cause NullPointerException when RubyBasicObject.metaClass is null
            String typeName = value.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(value));
            appendWithMaxLength(sb, typeName, taintValueLength);
        }
        return sb.toString();
    }

    @Deprecated
    private static void appendWithMaxLength(StringBuilder sb, String content, int maxLength) {
        if (sb.length() + content.length() > maxLength) {
            int remainingSpace = maxLength - sb.length();
            if (remainingSpace > 0) {
                sb.append(content, 0, remainingSpace);
            }
        } else {
            sb.append(content);
        }
    }

    public List<Object> getStacks() {
        return stacks;
    }

    public void setStacks(StackTraceElement[] stackTraceElements) {
        List<Object> stacks = new ArrayList<>();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            stacks.add(stackTraceElement.toString());
        }
        this.stacks = stacks;
    }

    /**
     * 把对象格式化为字符串，高频调用要尽可能快
     *
     * @param value 要转换为字符串的对象
     * @return
     */
    public static ObjectFormatResult formatObject(Object value) {
        // TODO 2023-9-5 11:49:14 晚点再看看要不要把这个方法也下沉到commons中，目前是因为有依赖无法下沉...
        return ObjectFormatter.formatObject(value, PropertyUtils.getTaintToStringCharLimit());
    }

}
