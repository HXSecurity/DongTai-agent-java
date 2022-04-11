package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractClassVisitor extends ClassVisitor {

    protected IastContext context;
    private boolean transformed;

    public AbstractClassVisitor(ClassVisitor classVisitor, IastContext context) {
        super(AsmUtils.api, classVisitor);
        this.context = context;
        this.transformed = false;
    }

    public void setTransformed() {
        transformed = true;
    }

    public boolean isTransformed() {
        return transformed;
    }

    public boolean hasTransformed() {
        return transformed;
    }
}
