package io.dongtai.iast.core.handler.hookpoint.models;

/**
 * 传播方法策略模型
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IastPropagatorModel {
    private final String type;
    private final String signature;
    private final Object sourcePosition;
    private final String source;
    private final String target;
    private final Object targetPosition;

    public IastPropagatorModel(String type, String signature, String source, Object sourcePosition, String target, Object targetPosition) {
        this.type = type;
        this.signature = signature;
        this.source = source;
        this.sourcePosition = sourcePosition;
        this.target = target;
        this.targetPosition = targetPosition;
    }


    public String getSignature() {
        return signature;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Object getTargetPosition() {
        return targetPosition;
    }

    public Object getSourcePosition() {
        return sourcePosition;
    }

    public String getType() {
        return type;
    }

}
