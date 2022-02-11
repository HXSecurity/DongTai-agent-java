package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.jsp;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import io.dongtai.log.DongTaiLog;

/**
 * 处理jsp include方法的文件包含
 *
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchJsp implements DispatchPlugin {

    private static final String JSP_PAGE = " javax.servlet.jsp.JspPage".substring(1);
    private static final String JSP_BASE = " org.apache.jasper.runtime.HttpJspBase".substring(1);
    private Set<String> ancestors;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        ancestors = context.getAncestors();

        String matchClassname = isMatch();
        if (null != matchClassname) {
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("JspPage match class for {} from {}", context.getClassName(), matchClassname);
            }
            context.setMatchClassName(matchClassname);
            // JspPageAdapter
            classVisitor = new JspPageAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        if (ancestors.contains(JSP_BASE)) {
            return JSP_BASE;
        } else if (ancestors.contains(JSP_PAGE)) {
            return JSP_PAGE;
        }
        return null;
    }
}
