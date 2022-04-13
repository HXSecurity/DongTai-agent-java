package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.cookie.DispatchCookie;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.DispatchClassPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo.DispatchDubbo;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc.DispatchGrpc;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch.DispatchJ2ee;
import io.dongtai.iast.core.bytecode.enhance.plugin.hardcoded.DispatchHardcodedPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.service.jdbc.DispatchJdbc;
import io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka.DispatchKafka;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.DispatchSpringApplication;
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
        this.plugins.add(new DispatchCookie());
        this.plugins.add(new DispatchDubbo());
        this.plugins.add(new DispatchKafka());
        this.plugins.add(new DispatchJdbc());

        //PLUGINS.add(new DispatchSpringAutoBinding());
        this.plugins.add(new DispatchClassPlugin());
        plugins.add(new DispatchGrpc());
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
