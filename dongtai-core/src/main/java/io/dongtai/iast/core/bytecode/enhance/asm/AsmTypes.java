package io.dongtai.iast.core.bytecode.enhance.asm;

import java.lang.dongtai.SpyDispatcher;
import java.lang.dongtai.SpyDispatcherHandler;
import org.objectweb.asm.Type;

/**
 * 常用的ASM type集合 省得我到处声明
 *
 * @author luanjia@taobao.com
 * @modify dongzhiyong@huoxian.cn
 */
public interface AsmTypes {

    Type ASM_TYPE_SPY_DISPATCHER = Type.getType(SpyDispatcher.class);
    Type ASM_TYPE_SPY_HANDLER = Type.getType(SpyDispatcherHandler.class);
    Type ASM_TYPE_THROWABLE = Type.getType(Throwable.class);
    Type ASM_TYPE_OBJECT = Type.getType(Object.class);
}
