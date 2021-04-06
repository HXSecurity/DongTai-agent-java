package com.secnium.iast.core.enhance.plugins.sinks.cookie;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.types.BaseType;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CookieAdapter extends BaseType {
    public CookieAdapter(ClassVisitor classVisitor, IASTContext context) {
        super(classVisitor, context, null);
    }

    @Override
    protected boolean match(String name, String classname) {
        return DispatchCookie.isHookMethod(name);
    }
}
