package com.secnium.iast.core.enhance.plugins;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.framework.j2ee.dispatch.DispatchJ2ee;
import com.secnium.iast.core.enhance.plugins.framework.j2ee.jsp.DispatchJsp;
import com.secnium.iast.core.enhance.plugins.framework.tomcat.DispatchRecyclePlugin;
import com.secnium.iast.core.enhance.plugins.sinks.autobinding.DispatchSpringAutoBinding;
import com.secnium.iast.core.enhance.plugins.sinks.cookie.DispatchCookie;
import com.secnium.iast.core.enhance.plugins.sources.servlet.DispatchServlet;
import com.secnium.iast.core.enhance.plugins.sources.servlet.stream.catalina.DispatchCoyoteInputStream;
import com.secnium.iast.core.enhance.plugins.sources.spring.DispatchSpringSource;
import com.secnium.iast.core.enhance.plugins.sources.struts2.DispatchStruts2;
import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PluginRegister {

    /**
     * 定义PLUGINS常量，用于存储定义的字节码修改类
     */
    private final static ArrayList<DispatchPlugin> PLUGINS;

    public ClassVisitor initial(ClassVisitor classVisitor, IastContext context) {
        // todo 暂时注释硬编码检测实现类，后续看情况打开。classVisitor = new DispatchHardcodedPlugin().dispatch(classVisitor, context);
        for (DispatchPlugin plugin : PLUGINS) {
            ClassVisitor pluginVisitor = plugin.dispatch(classVisitor, context);
            if (pluginVisitor != classVisitor) {
                classVisitor = pluginVisitor;
                break;
            }
        }
        return classVisitor;
    }

    static {
        PLUGINS = new ArrayList<DispatchPlugin>();
        PLUGINS.add(new DispatchTechnologyPlugin());
        PLUGINS.add(new DispatchSpringSource());
        PLUGINS.add(new DispatchJ2ee());
        PLUGINS.add(new DispatchJsp());
        PLUGINS.add(new DispatchServlet());
        PLUGINS.add(new DispatchStruts2());
        PLUGINS.add(new DispatchCoyoteInputStream());
        PLUGINS.add(new DispatchCookie());
        PLUGINS.add(new DispatchRecyclePlugin());

        PLUGINS.add(new DispatchSpringAutoBinding());

        PLUGINS.add(new DispatchClassPlugin());
    }
}
