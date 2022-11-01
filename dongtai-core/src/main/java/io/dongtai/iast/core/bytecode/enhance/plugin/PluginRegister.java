package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.authentication.shiro.DispatchShiro;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.DispatchClassPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch.DispatchJ2ee;
import io.dongtai.iast.core.bytecode.enhance.plugin.hardcoded.DispatchHardcodedPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.DispatchSpringApplication;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.List;

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
        // this.plugins.add(new DispatchCookie());
        // this.plugins.add(new DispatchDubbo());
        // this.plugins.add(new DispatchKafka());
        // this.plugins.add(new DispatchJdbc());
        this.plugins.add(new DispatchShiro());
//        this.plugins.add(new DispatchHandlerInterceptor());

        //PLUGINS.add(new DispatchSpringAutoBinding());
        this.plugins.add(new DispatchClassPlugin());
        // plugins.add(new DispatchGrpc());
    }

    public ClassVisitor initial(ClassVisitor classVisitor, ClassContext context, PolicyManager policyManager) {
        Policy policy = policyManager.getPolicy();
        if (policy == null) {
            return classVisitor;
        }

        classVisitor = new DispatchHardcodedPlugin().dispatch(classVisitor, context, policy);
        for (DispatchPlugin plugin : plugins) {
            ClassVisitor pluginVisitor = plugin.dispatch(classVisitor, context, policy);
            if (pluginVisitor != classVisitor) {
                classVisitor = pluginVisitor;
                break;
            }
        }
        return classVisitor;
    }
}
