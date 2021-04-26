package com.secnium.iast.core.enhance.plugins.framework.tomcat;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CharChunkAdapter extends AdviceAdapter {
    private final IastContext IastContext;

    CharChunkAdapter(MethodVisitor mv, int access, String name, String desc, IastContext IastContext) {
        super(AsmUtils.api, mv, access, name, desc);
        this.IastContext = IastContext;
    }

    @Override
    protected void onMethodExit(int opCode) {
        if (opCode != ATHROW) {
            // 通过动态代理实现封装
            loadThis();
        }
    }
}
