package com.secnium.iast.core.enhance.plugins.framework.j2ee.jsp;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import com.secnium.iast.core.util.LogUtils;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;

/**
 * 处理jsp include方法的文件包含
 *
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchJsp implements DispatchPlugin {

    private static final String JSP_PAGE = " javax.servlet.jsp.JspPage".substring(1);
    private static final String JSP_BASE = " org.apache.jasper.runtime.HttpJspBase".substring(1);
    private Set<String> ancestors;
    private final Logger logger = LogUtils.getLogger(getClass());

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        ancestors = context.getAncestors();

        String matchClassname = isMatch();
        if (null != matchClassname) {
            if (logger.isDebugEnabled()) {
                logger.debug("JspPage match class for {} from {}", context.getClassName(), matchClassname);
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
