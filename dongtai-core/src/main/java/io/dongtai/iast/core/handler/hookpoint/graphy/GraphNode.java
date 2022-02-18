package io.dongtai.iast.core.handler.hookpoint.graphy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

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

    private final String targetValues;

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
    private final List<Integer> sourceHash;
    /**
     * 输出污点hash
     */
    private final List<Integer> targetHash;

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
                     List<Integer> sourceHash,
                     List<Integer> targetHash,
                     String sourceValues,
                     String targetValues
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
        this.targetValues = targetValues;
    }

    public JSONObject toJson() {
        JSONObject value = new JSONObject();
        JSONArray interfaceArray = new JSONArray();
        JSONArray sourceHashArray = new JSONArray();
        JSONArray targetHashArray = new JSONArray();

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
        value.put("targetHash", targetHashArray);
        value.put("targetValues", targetValues);

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

        return value;
    }
}
