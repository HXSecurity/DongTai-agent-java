package io.dongtai.iast.core.handler.hookpoint.graphy;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
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
     * 标记是否为source方法
     */
    private final boolean isSource;
    /**
     * 方法调用ID
     */
    private final int invokeId;
    /**
     * 当前方法被调用方法的类名
     */
    private final String callerClass;
    /**
     * 当前方法被调用方法的名称
     */
    private final String callerMethod;
    /**
     * 当前方法在被调用方法中的行号
     */
    private final int callerLineNumber;

    private final String sourceValues;

    private final boolean sourceIsReference;

    private final String targetValues;

    private final boolean targetIsReference;

    /**
     * 当前方法所在类继承的类名称
     */
    public void setInterfaceNames(Set<String> interfaceNames) {
        if (interfaceNames == null) {
            this.interfaceNames = null;
        } else {
            int totals = interfaceNames.size();
            int index = 0;
            String[] interfaces = new String[totals];

            for (String interfaceName : interfaceNames) {
                interfaces[index++] = interfaceName;
            }
            this.interfaceNames = interfaces;
        }
    }

    /**
     * 当前方法所在类实现的接口列表
     */
    private String[] interfaceNames;

    /**
     * 方法所在类的原始名称
     */
    private final String matchClassName;
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
    /**
     * 当前方法的参数列表
     */
    private final String args;
    /**
     * 当前方法的返回值对应的类名称
     */
    private final String retClassName;
    /**
     * 来源污点hash
     */
    private final Set<Integer> sourceHash;
    /**
     * 输出污点hash
     */
    private final Set<Integer> targetHash;

    /**
     * 增加一组hashcode，用于处理rpc请求中，污点断链的情况
     *
     * @issue: http://
     */
    private final Set<Integer> sourceHashForRpc;

    /**
     * 增加一组hashcode，用于处理rpc请求中，污点断链的情况
     *
     * @issue: http://
     */
    private final Set<Integer> targetHashForRpc;

    private final String traceId;
    private final String serviceName;
    private final String plugin;
    private final Boolean projectPropagatorClose;

    private List<MethodEvent.MethodEventTargetRange> targetRanges = new ArrayList<MethodEvent.MethodEventTargetRange>();

    public List<MethodEvent.MethodEventSourceType> sourceTypes;

    public GraphNode(boolean isSource,
                     int invokeId,
                     String callerClass,
                     String callerMethod,
                     int callerLineNumber,
                     Set<String> interfaceNames,
                     String matchClassName,
                     String originClassName,
                     String methodName,
                     String signature,
                     String args,
                     String retClassName,
                     Set<Integer> sourceHash,
                     Set<Integer> targetHash,
                     String sourceValues,
                     boolean sourceIsReference,
                     String targetValues,
                     boolean targetIsReference,
                     Set<Integer> sourceHashForRpc,
                     Set<Integer> targetHashForRpc,
                     String traceId,
                     String serviceName,
                     String plugin,
                     Boolean projectPropagatorClose,
                     List<MethodEvent.MethodEventTargetRange> targetRanges,
                     List<MethodEvent.MethodEventSourceType> sourceTypes
    ) {
        this.isSource = isSource;
        this.invokeId = invokeId;
        this.callerClass = callerClass;
        this.callerMethod = callerMethod;
        this.callerLineNumber = callerLineNumber;
        this.setInterfaceNames(interfaceNames);
        this.matchClassName = matchClassName;
        this.originClassName = originClassName;
        this.methodName = methodName;
        this.signature = signature;
        this.args = args;
        this.retClassName = retClassName;
        this.sourceHash = sourceHash;
        this.targetHash = targetHash;
        this.sourceValues = sourceValues;
        this.sourceIsReference = sourceIsReference;
        this.targetValues = targetValues;
        this.targetIsReference = targetIsReference;
        this.sourceHashForRpc = sourceHashForRpc;
        this.targetHashForRpc = targetHashForRpc;
        this.traceId = traceId;
        this.serviceName = serviceName;
        this.plugin = plugin;
        this.projectPropagatorClose = projectPropagatorClose;
        this.targetRanges = targetRanges;
        this.sourceTypes = sourceTypes;
    }

    public JSONObject toJson() {
        JSONObject value = new JSONObject();
        JSONArray interfaceArray = new JSONArray();
        JSONArray sourceHashArray = new JSONArray();
        JSONArray targetHashArray = new JSONArray();
        JSONArray sourceHashForRpcArray = new JSONArray();
        JSONArray targetHashForRpcArray = new JSONArray();

        value.put("source", isSource);
        value.put("invokeId", invokeId);
        value.put("callerClass", callerClass);
        value.put("callerMethod", callerMethod);
        value.put("callerLineNumber", callerLineNumber);
        value.put("interfaces", interfaceArray);
        value.put("originClassName", originClassName);
        value.put("className", matchClassName);
        value.put("methodName", methodName);
        value.put("signature", signature);
        value.put("args", args);
        value.put("retClassName", retClassName);
        value.put("sourceHash", sourceHashArray);
        value.put("sourceValues", sourceValues);
        value.put("sourceIsReference",sourceIsReference);
        value.put("targetHash", targetHashArray);
        value.put("targetIsReference", targetIsReference);
        value.put("targetValues", targetValues);
        value.put("sourceHashForRpc", sourceHashForRpcArray);
        value.put("targetHashForRpc", targetHashForRpcArray);
        value.put("traceId", traceId);
        value.put("serviceName", serviceName);
        value.put("plugin", plugin);
        value.put("projectPropagatorClose", projectPropagatorClose);


        if (interfaceNames != null) {
            for (String interfaceName : interfaceNames) {
                interfaceArray.put(interfaceName);
            }
        }

        for (Integer hash : sourceHash) {
            sourceHashArray.put(hash);
        }

        for (Integer hash : targetHash) {
            targetHashArray.put(hash);
        }

        for (Integer hash : this.sourceHashForRpc) {
            sourceHashForRpcArray.put(hash);
        }

        for (Integer hash : this.targetHashForRpc) {
            targetHashForRpcArray.put(hash);
        }

        JSONArray tr = new JSONArray();
        value.put("targetRange", tr);
        for (MethodEvent.MethodEventTargetRange range : targetRanges) {
            tr.put(range.toJson());
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
