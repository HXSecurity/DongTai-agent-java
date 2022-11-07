package io.dongtai.iast.core.handler.hookpoint.graphy;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * 图节点，用于服务器端污点方法图的构造
 *
 * @author dongzhiyong@huoxian.cn
 */
public class GraphNode {
    /**
     * is source policy
     */
    private final boolean isSource;
    /**
     * method invoke id
     */
    private final int invokeId;
    private List<String> sourcePositions = new ArrayList<String>();
    private List<String> targetPositions = new ArrayList<String>();
    /**
     * current method caller class name
     */
    private final String callerClass;
    /**
     * current method caller method name
     */
    private final String callerMethod;
    /**
     * current method caller file line number
     */
    private final int callerLineNumber;

    /**
     * 方法所在类的原始名称
     */
    private final String matchedClassName;
    /**
     * 当前方法所在类的名称
     */
    private final String originClassName;
    /**
     * 当前方法的名称
     */
    private final String methodName;
    /**
     * 当前方法的方法签名
     */
    private final String signature;
    private final String objectValue;
    private final List<MethodEvent.Parameter> parameterValues;
    private final String returnValue;
    /**
     * 来源污点hash
     */
    private final Set<Integer> sourceHash;
    /**
     * 输出污点hash
     */
    private final Set<Integer> targetHash;

    private List<MethodEvent.MethodEventTargetRange> targetRanges = new ArrayList<MethodEvent.MethodEventTargetRange>();

    public List<MethodEvent.MethodEventSourceType> sourceTypes;

    public GraphNode(MethodEvent event) {
        this.isSource = event.isSource();
        this.invokeId = event.getInvokeId();
        if (event.getSourcePositions() != null && event.getSourcePositions().size() > 0) {
            for (TaintPosition src : event.getSourcePositions()) {
                this.sourcePositions.add(src.toString());
            }
        }
        if (event.getTargetPositions() != null && event.getTargetPositions().size() > 0) {
            for (TaintPosition tgt : event.getTargetPositions()) {
                this.targetPositions.add(tgt.toString());
            }
        }

        this.matchedClassName = event.getMatchedClassName();
        this.originClassName = event.getOriginClassName();
        this.methodName = event.getMethodName();
        this.signature = event.getSignature();
        this.objectValue = event.objectValue;
        this.parameterValues = event.parameterValues;
        this.returnValue = event.returnValue;
        this.callerClass = event.getCallerClass();
        this.callerMethod = event.getCallerMethod();
        this.callerLineNumber = event.getCallerLine();
        this.sourceHash = event.getSourceHashes();
        this.targetHash = event.getTargetHashes();
        this.targetRanges = event.targetRanges;
        this.sourceTypes = event.sourceTypes;
    }

    public JSONObject toJson() {
        JSONObject value = new JSONObject();
        JSONArray parameterArray = new JSONArray();
        JSONArray sourceHashArray = new JSONArray();
        JSONArray targetHashArray = new JSONArray();
        JSONObject taintPosition = new JSONObject();

        value.put("invokeId", invokeId);
        value.put("source", isSource);
        value.put("originClassName", originClassName);
        value.put("className", matchedClassName);
        value.put("methodName", methodName);
        value.put("signature", signature);
        value.put("callerClass", callerClass);
        value.put("callerMethod", callerMethod);
        value.put("callerLineNumber", callerLineNumber);
        value.put("sourceHash", sourceHashArray);
        value.put("targetHash", targetHashArray);

        value.put("taintPosition", taintPosition);
        if (this.sourcePositions.size() > 0) {
            taintPosition.put("source", this.sourcePositions);
        }
        if (this.targetPositions.size() > 0) {
            taintPosition.put("target", this.targetPositions);
        }

        if (!StringUtils.isEmpty(this.objectValue)) {
            value.put("objValue", objectValue);
        }
        if (this.parameterValues != null && this.parameterValues.size() > 0) {
            for (MethodEvent.Parameter parameter : this.parameterValues) {
                parameterArray.put(parameter.toJson());
            }
            value.put("parameterValues", parameterArray);
        }
        if (!StringUtils.isEmpty(this.returnValue)) {
            value.put("retValue", returnValue);
        }

        for (Integer hash : sourceHash) {
            sourceHashArray.put(hash);
        }

        for (Integer hash : targetHash) {
            targetHashArray.put(hash);
        }

        if (targetRanges.size() > 0) {
            JSONArray tr = new JSONArray();
            value.put("targetRange", tr);
            for (MethodEvent.MethodEventTargetRange range : targetRanges) {
                tr.put(range.toJson());
            }
        }

        if (sourceTypes != null && sourceTypes.size() > 0) {
            JSONArray st = new JSONArray();
            value.put("sourceType", st);
            for (MethodEvent.MethodEventSourceType s : sourceTypes) {
                st.put(s.toJson());
            }
        }

        return value;
    }
}
