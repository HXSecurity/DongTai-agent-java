package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractClassVisitor extends ClassVisitor {

    protected ClassContext context;
    protected Policy policy;
    private boolean transformed;

    public AbstractClassVisitor(ClassVisitor classVisitor, ClassContext context) {
        super(AsmUtils.api, classVisitor);
        this.context = context;
        this.transformed = false;
    }

    public AbstractClassVisitor(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        super(AsmUtils.api, classVisitor);
        this.context = context;
        this.policy = policy;
        this.transformed = false;
    }

    public void setTransformed() {
        transformed = true;
    }

    public boolean hasTransformed() {
        return transformed;
    }
}
