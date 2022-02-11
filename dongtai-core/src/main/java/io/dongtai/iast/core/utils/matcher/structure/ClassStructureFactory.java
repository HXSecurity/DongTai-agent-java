package io.dongtai.iast.core.utils.matcher.structure;


import io.dongtai.log.DongTaiLog;

import java.io.IOException;
import java.io.InputStream;

/**
 * 类结构工厂类
 * <p>
 * 根据构造方式的不同，返回的实现方式也不一样。但无论哪一种实现方式都尽可能符合接口约定。
 * </p>
 *
 * @author luanjia@taobao.com
 */
public class ClassStructureFactory {

    /**
     * 通过Class类字节流来构造类结构
     *
     * @param classInputStream Class类字节流
     * @param loader           即将装载Class的ClassLoader
     * @return ASM实现的类结构
     */
    public static ClassStructure createClassStructure(final InputStream classInputStream,
                                                      final ClassLoader loader) {
        try {
            return new ClassStructureImplByAsm(classInputStream, loader);
        } catch (IOException cause) {
            DongTaiLog.warn("create class structure failed by using ASM, return null. loader=" + loader + ";", cause);
            return null;
        }
    }

}
