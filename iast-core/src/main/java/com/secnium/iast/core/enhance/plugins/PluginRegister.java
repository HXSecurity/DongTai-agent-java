package com.secnium.iast.core.enhance.plugins;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.api.spring.DispatchSpringApplication;
import com.secnium.iast.core.enhance.plugins.cookie.DispatchCookie;
import com.secnium.iast.core.enhance.plugins.core.DispatchClassPlugin;
import com.secnium.iast.core.enhance.plugins.framework.dubbo.DispatchDubbo;
import com.secnium.iast.core.enhance.plugins.framework.j2ee.dispatch.DispatchJ2ee;
import com.secnium.iast.core.enhance.plugins.hardcoded.DispatchHardcodedPlugin;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PluginRegister {

    /**
     * 定义PLUGINS常量，用于存储定义的字节码修改类
     */
    private final List<DispatchPlugin> plugins;

    public PluginRegister() {
        this.plugins = new ArrayList<DispatchPlugin>();
        this.plugins.add(new DispatchSpringApplication());
        this.plugins.add(new DispatchJ2ee());
        //PLUGINS.add(new DispatchJsp());
        this.plugins.add(new DispatchCookie());
        this.plugins.add(new DispatchDubbo());
        //PLUGINS.add(new DispatchSpringAutoBinding());
        this.plugins.add(new DispatchClassPlugin());
        //PLUGINS.add()
    }

    public ClassVisitor initial(ClassVisitor classVisitor, IastContext context) {
        // todo 暂时注释硬编码检测实现类，后续看情况打开。
        classVisitor = new DispatchHardcodedPlugin().dispatch(classVisitor, context);
        for (DispatchPlugin plugin : plugins) {
            ClassVisitor pluginVisitor = plugin.dispatch(classVisitor, context);
            if (pluginVisitor != classVisitor) {
                classVisitor = pluginVisitor;
                break;
            }
        }
        return classVisitor;
    }
}
