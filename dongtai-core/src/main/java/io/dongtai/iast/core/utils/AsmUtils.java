package io.dongtai.iast.core.utils;

import io.dongtai.iast.core.utils.matcher.structure.ClassStructure;
import io.dongtai.iast.core.utils.matcher.structure.ClassStructureFactory;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.InputStream;

import static io.dongtai.iast.core.utils.SandboxStringUtils.toInternalClassName;

/**
 * ASM工具集
 *
 * @author luanjia@taobao.com
 * @Modify dongzhiyong@huoxian.cn
 */
public class AsmUtils {

    /**
     * 用于字节码修改的ASM版本，jdk 1.8及以下，使用ASM 5；jdk 1.9及以上，使用ASM 7
     */
    public static Integer api = Opcodes.ASM9;

    public static Type voidType = Type.getType(void.class);
    public static Type stringArrayType = Type.getType(String[].class);

    /**
     * just the same
     * {@code org.objectweb.asm.ClassWriter#getCommonSuperClass(String, String)}
     */
    public static String getCommonSuperClass(String type1, String type2, ClassLoader loader) {
        return getCommonSuperClassImplByAsm(type1, type2, loader);
    }

    /**
     * implements by ASM
     *
     * @param type1
     * @param type2
     * @param targetClassLoader
     * @return
     */
    private static String getCommonSuperClassImplByAsm(String type1, String type2, ClassLoader targetClassLoader) {
        InputStream inputStreamOfType1 = null, inputStreamOfType2 = null;
        try {
            //targetClassLoader 为null，说明是BootStrapClassLoader，不能显式引用，故使用系统类加载器间接引用
            if (null == targetClassLoader) {
                targetClassLoader = ClassLoader.getSystemClassLoader();
            }
            if (null == targetClassLoader) {
                return "java/lang/Object";
            }
            inputStreamOfType1 = targetClassLoader.getResourceAsStream(type1 + ".class");
            if (null == inputStreamOfType1) {
                return "java/lang/Object";
            }
            inputStreamOfType2 = targetClassLoader.getResourceAsStream(type2 + ".class");
            if (null == inputStreamOfType2) {
                return "java/lang/Object";
            }
            final ClassStructure classStructureOfType1 = ClassStructureFactory.createClassStructure(inputStreamOfType1, targetClassLoader);
            final ClassStructure classStructureOfType2 = ClassStructureFactory.createClassStructure(inputStreamOfType2, targetClassLoader);
            if (classStructureOfType2.getFamilyTypeClassStructures().contains(classStructureOfType1)) {
                return type1;
            }
            if (classStructureOfType1.getFamilyTypeClassStructures().contains(classStructureOfType2)) {
                return type2;
            }
            if (classStructureOfType1.getAccess().isInterface()
                    || classStructureOfType2.getAccess().isInterface()) {
                return "java/lang/Object";
            }
            ClassStructure classStructure = classStructureOfType1;
            do {
                classStructure = classStructure.getSuperClassStructure();
                if (null == classStructure) {
                    return "java/lang/Object";
                }
            } while (!classStructureOfType2.getFamilyTypeClassStructures().contains(classStructure));
            return toInternalClassName(classStructure.getJavaClassName());
        } finally {
            IOUtils.closeQuietly(inputStreamOfType1);
            IOUtils.closeQuietly(inputStreamOfType2);
        }
    }

    public static String[] buildParameterTypes(String desc) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        if (argTypes.length == 0) {
            return new String[]{};
        }
        String[] args = new String[argTypes.length];
        for (byte index = 0; index < argTypes.length; index++) {
            args[index] = argTypes[index].getClassName();
        }
        return args;
    }

    public static String buildSignature(String className, String methodName, String desc) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append(".");
        sb.append(methodName);
        sb.append("(");
        for (byte index = 0; index < argTypes.length; index++) {
            sb.append(argTypes[index].getClassName());
            if (index != argTypes.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
