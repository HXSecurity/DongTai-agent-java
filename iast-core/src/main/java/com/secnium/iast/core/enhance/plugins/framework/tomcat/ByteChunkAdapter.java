package com.secnium.iast.core.enhance.plugins.framework.tomcat;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class ByteChunkAdapter extends AdviceAdapter {
    private final IASTContext IASTContext;

    ByteChunkAdapter(MethodVisitor mv, int access, String name, String desc, IASTContext IASTContext) {
        super(AsmUtils.api, mv, access, name, desc);
        this.IASTContext = IASTContext;
    }

    @Override
    protected void onMethodExit(int opCode) {
        if (opCode != ATHROW) {
            // 通过动态代理实现封装
            loadThis();
        }
    }
}
