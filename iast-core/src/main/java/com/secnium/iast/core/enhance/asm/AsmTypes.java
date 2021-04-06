package com.secnium.iast.core.enhance.asm;

import org.objectweb.asm.Type;

import java.lang.iast.inject.Injecter;

/**
 * 常用的ASM type集合
 * 省得我到处声明
 *
 * @author luanjia@taobao.com
 * @modify dongzhiyong@huoxian.cn
 */
public interface AsmTypes {

    Type ASM_TYPE_SPY = Type.getType(Injecter.class);
    Type ASM_TYPE_THROWABLE = Type.getType(Throwable.class);
}
