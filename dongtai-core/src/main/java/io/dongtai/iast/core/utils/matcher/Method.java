package io.dongtai.iast.core.utils.matcher;

import static io.dongtai.iast.core.utils.AsmUtils.stringArrayType;
import static io.dongtai.iast.core.utils.AsmUtils.voidType;

import java.lang.reflect.Modifier;
import org.objectweb.asm.Type;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class Method {

    public static boolean hook(int access, String name, String desc, String signature) {
        return Modifier.isAbstract(access) ||
                "clone".equals(name) ||
                "equals".equals(name) ||
                "finalize".equals(name) ||
                "getClass".equals(name) ||
                "hashCode".equals(name) ||
                "notify".equals(name) ||
                "notifyAll".equals(name) ||
                "toString".equals(name) ||
                "wait".equals(name) ||
                "<clinit>".equals(name) ||
                "<init>".equals(name) ||
                Modifier.isNative(access) ||
                isMain(access, name, desc) ||
                ConfigMatcher.getInstance().blackFunc(signature).equals(ConfigMatcher.PropagatorType.BLACK);
    }

    private static boolean isMain(int access, String name, String desc) {
        return Modifier.isPublic(access)
                && Modifier.isStatic(access)
                && Type.getReturnType(desc).equals(voidType)
                && "main".equals(name)
                && Type.getArgumentTypes(desc).length == 1
                && Type.getArgumentTypes(desc)[0].equals(stringArrayType);
    }
}
