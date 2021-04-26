package com.secnium.iast.core.enhance.plugins;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractClassVisitor extends ClassVisitor {

    protected IastContext context;
    protected boolean transformed;

    public AbstractClassVisitor(ClassVisitor classVisitor, IastContext context) {
        super(AsmUtils.api, classVisitor);
        this.context = context;
        this.transformed = false;
    }

    public abstract boolean hasTransformed();
}
