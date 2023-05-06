package io.dongtai.iast.core.bytecode.enhance.plugin.framework.feign;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchFeign implements DispatchPlugin {
    public static final String FEIGN_SYNC_HANDLER = "feign.SynchronousMethodHandler";

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();

        if (FEIGN_SYNC_HANDLER.equals(className)) {
            classVisitor = new FeignSyncHandlerAdapter(classVisitor, context);
        }

        return classVisitor;
    }

    @Override
    public String getName() {
        return "feign";
    }
}
