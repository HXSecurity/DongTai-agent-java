package io.dongtai.iast.core.bytecode.enhance.plugin.service.jdbc;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.handler.hookpoint.service.ServiceType;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class MysqlJdbcDriverParseUrlAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    protected MysqlJdbcDriverParseUrlAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(AsmUtils.api, mv, access, name, desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            newLocal(ASM_TYPE_OBJECT);
            dup();
            storeLocal(nextLocal - 1);
            Label nonNullLabel = new Label();
            loadLocal(nextLocal - 1);
            ifNull(nonNullLabel);
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(ServiceType.MYSQL.getCategory());
            push(ServiceType.MYSQL.getType());
            loadLocal(nextLocal - 1);
            push("HOST");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Properties", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            loadLocal(nextLocal - 1);
            push("PORT");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Properties", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
            push("");
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$reportService);
            mark(nonNullLabel);
        }
    }
}
