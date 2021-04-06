package com.secnium.iast.core.enhance.plugins.framework.tomcat;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class RequestAdapter extends AdviceAdapter {
    private final IASTContext iastContext;

    RequestAdapter(MethodVisitor mv, int access, String name, String desc, IASTContext IASTContext) {
        super(AsmUtils.api, mv, access, name, desc);
        this.iastContext = IASTContext;
    }

    @Override
    protected void onMethodEnter() {
        // 标记开始
    }

    @Override
    protected void onMethodExit(int opCode) {
        // 标记结束
    }
}
