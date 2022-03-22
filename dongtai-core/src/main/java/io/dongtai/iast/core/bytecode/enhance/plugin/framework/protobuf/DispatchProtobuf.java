package io.dongtai.iast.core.bytecode.enhance.plugin.framework.protobuf;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchProtobuf implements DispatchPlugin {
    private static final String classOfByteString = "com.google.protobuf.ByteString";

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();
        switch (className) {
            case classOfByteString:
                classVisitor = new ByteStringAdapter(classVisitor, null);
                break;
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
