package io.dongtai.iast.core.bytecode.enhance.plugin.cookie;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CookieAdapter extends BaseType {
    public CookieAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context, null);
    }

    @Override
    protected boolean match(String name, String classname) {
        return DispatchCookie.isHookMethod(name);
    }
}
