package com.secnium.iast.core.enhance.plugins.cookie;

import com.secnium.iast.core.enhance.IastContext;
import org.objectweb.asm.ClassVisitor;
import com.secnium.iast.core.enhance.plugins.cookie.BaseType;

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
