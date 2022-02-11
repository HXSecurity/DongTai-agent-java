package io.dongtai.iast.core.bytecode.enhance.plugin.framework.tomcat;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class RequestAdapter extends AdviceAdapter {
    private final IastContext iastContext;

    RequestAdapter(MethodVisitor mv, int access, String name, String desc, IastContext IastContext) {
        super(AsmUtils.api, mv, access, name, desc);
        this.iastContext = IastContext;
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
