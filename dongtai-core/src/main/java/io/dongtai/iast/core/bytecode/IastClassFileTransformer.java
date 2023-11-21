package io.dongtai.iast.core.bytecode;

import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.IastClassDiagram;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.PluginRegister;
import io.dongtai.iast.core.bytecode.sca.ScaScanner;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.FastjsonCheck;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.QLExpressCheck;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.matcher.ConfigMatcher;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.apache.commons.lang3.time.StopWatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.lang.dongtai.SpyDispatcherHandler;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastClassFileTransformer implements ClassFileTransformer {

    private final IastClassDiagram classDiagram;

    private int transformCount = 0;
    private final boolean isDumpClass;
    private final ConfigMatcher configMatcher;
    private final Instrumentation inst;
    private final PropertyUtils properties;
    private final PluginRegister plugins;
    private static IastClassFileTransformer INSTANCE;
    private final PolicyManager policyManager;
    private final static HashMap<Object, byte[]> transformMap = new HashMap<Object, byte[]>();

    /**
     * Gets a singleton object
     *
     * @param inst Instrumentation object
     * @return ClassFileTransformer object
     * @since 1.3.1
     */
    public static IastClassFileTransformer getInstance(Instrumentation inst, PolicyManager policyManager) {
        if (null == INSTANCE) {
            INSTANCE = new IastClassFileTransformer(inst, policyManager);
        }
        return INSTANCE;
    }

    public static IastClassFileTransformer getInstance() {
        if (null != INSTANCE) {
            return INSTANCE;
        }
        return null;
    }

    IastClassFileTransformer(Instrumentation inst, PolicyManager policyManager) {
        this.inst = inst;
        this.isDumpClass = EngineManager.getInstance().isEnableDumpClass();
        this.properties = PropertyUtils.getInstance();
        this.classDiagram = IastClassDiagram.getInstance();
        this.plugins = new PluginRegister();
        this.configMatcher = ConfigMatcher.getInstance();
        this.configMatcher.setInst(inst);
        this.policyManager = policyManager;

        SpyDispatcherHandler.setDispatcher(new SpyDispatcherImpl());
    }

    public int getTransformCount() {
        return transformCount;
    }

    /**
     * The implementation of this method may transform the supplied class file and return a new replacement class file.
     * There are two kinds of transformers, determined by the canRetransform parameter of Instrumentation.addTransformer(ClassFileTransformer, boolean):
     * retransformation capable transformers that were added with canRetransform as true
     * retransformation incapable transformers that were added with canRetransform as false or where added with Instrumentation.addTransformer(ClassFileTransformer)
     * Once a transformer has been registered with addTransformer, the transformer will be called for every new class definition and every class redefinition. Retransformation capable transformers will also be called on every class retransformation. The request for a new class definition is made with ClassLoader.defineClass or its native equivalents. The request for a class redefinition is made with Instrumentation.redefineClasses or its native equivalents. The request for a class retransformation is made with Instrumentation.retransformClasses or its native equivalents. The transformer is called during the processing of the request, before the class file bytes have been verified or applied. When there are multiple transformers, transformations are composed by chaining the transform calls. That is, the byte array returned by one call to transform becomes the input (via the classfileBuffer parameter) to the next call.
     * Transformations are applied in the following order:
     * Retransformation incapable transformers
     * Retransformation incapable native transformers
     * Retransformation capable transformers
     * Retransformation capable native transformers
     * For retransformations, the retransformation incapable transformers are not called, instead the result of the previous transformation is reused. In all other cases, this method is called. Within each of these groupings, transformers are called in the order registered. Native transformers are provided by the ClassFileLoadHook event in the Java Virtual Machine Tool Interface).
     * The input (via the classfileBuffer parameter) to the first transformer is:
     * for new class definition, the bytes passed to ClassLoader.defineClass
     * for class redefinition, definitions.getDefinitionClassFile() where definitions is the parameter to Instrumentation.redefineClasses
     * for class retransformation, the bytes passed to the new class definition or, if redefined, the last redefinition, with all transformations made by retransformation incapable transformers reapplied automatically and unaltered; for details see Instrumentation.retransformClasses
     * If the implementing method determines that no transformations are needed, it should return null. Otherwise, it should create a new byte[] array, copy the input classfileBuffer into it, along with all desired transformations, and return the new array. The input classfileBuffer must not be modified.
     * In the retransform and redefine cases, the transformer must support the redefinition semantics: if a class that the transformer changed during initial definition is later retransformed or redefined, the transformer must insure that the second class output class file is a legal redefinition of the first output class file.
     * If the transformer throws an exception (which it doesn't catch), subsequent transformers will still be called and the load, redefine or retransform will still be attempted. Thus, throwing an exception has the same effect as returning null. To prevent unexpected behavior when unchecked exceptions are generated in transformer code, a transformer can catch Throwable. If the transformer believes the classFileBuffer does not represent a validly formatted class file, it should throw an IllegalClassFormatException; while this has the same effect as returning null. it facilitates the logging or debugging of format corruptions.
     *
     * @param loader              the defining loader of the class to be transformed, may be null if the bootstrap loader
     * @param internalClassName   he name of the class in the internal form of fully qualified class and interface names as defined in The Java Virtual Machine Specification. For example, "java/util/List".
     * @param classBeingRedefined if this is triggered by a redefine or retransform, the class being redefined or retransformed; if this is a class load, null
     * @param protectionDomain    the protection domain of the class being defined or redefined
     * @param srcByteCodeArray    the input byte buffer in class file format - must not be modified
     * @return 修改后的字节码，为null时，不进行修改
     */
    @Override
    public byte[] transform(final ClassLoader loader,
                            final String internalClassName,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] srcByteCodeArray) {

        // TODO 那下面这段逻辑是否就可以去掉了？
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("DongTai-IAST-Core")) {
            return null;
        }

        if (internalClassName == null
                || internalClassName.startsWith("io/dongtai/")
                || internalClassName.startsWith("java/lang/iast/")
                || internalClassName.startsWith("META-INF/")
                || "module-info".equals(internalClassName)) {
            return null;
        }

        if (null != loader && loader.toString().toLowerCase().contains("rasp")) {
            return null;
        }

        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            if (" com/ql/util/express/config/QLExpressRunStrategy".substring(1).equals(internalClassName)){
                QLExpressCheck.setQLClassLoader(loader);
            }
            if (" com/alibaba/fastjson/JSON".substring(1).equals(internalClassName)) {
                FastjsonCheck.setJsonClassLoader(loader);
            } else if (" com/alibaba/fastjson/parser/ParserConfig".substring(1).equals(internalClassName)) {
                FastjsonCheck.setParseConfigClassLoader(loader);
            }

            if (loader != null && protectionDomain != null) {
                final CodeSource codeSource = protectionDomain.getCodeSource();
                if (codeSource == null) {
                    return null;
                }
                URL location = codeSource.getLocation();
                if (location != null && !internalClassName.startsWith("sun/") && !location.getFile().isEmpty()) {
                    ScaScanner.scanForSCA(location.getFile(), internalClassName);
                }
            }
            //判断是否可以hook ，true可以 false不可以
            if (null == classBeingRedefined && !configMatcher.canHook(internalClassName, this.policyManager)) {
                return null;
            }

            byte[] sourceCodeBak = new byte[srcByteCodeArray.length];
            System.arraycopy(srcByteCodeArray, 0, sourceCodeBak, 0, srcByteCodeArray.length);
            final ClassReader cr = new ClassReader(sourceCodeBak);

            ClassContext classContext = new ClassContext(cr, loader);
            if (Modifier.isInterface(classContext.getModifier())) {
                return null;
            }
            final String className = classContext.getClassName();

            // 设置类的祖先类
            Set<String> ancestors = classDiagram.getClassAncestorSet(className);
            if (ancestors == null) {
                ancestors = classDiagram.updateAncestorsByClassContext(loader, classContext);
            }
            classContext.setAncestors(ancestors);

            final ClassWriter cw = createClassWriter(loader, cr);
            ClassVisitor cv = plugins.initial(cw, classContext, policyManager);

            if (cv instanceof AbstractClassVisitor) {
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
                AbstractClassVisitor dumpClassVisitor = (AbstractClassVisitor) cv;
                if (dumpClassVisitor.hasTransformed()) {
                    if (null == classBeingRedefined) {
                        transformMap.put(className, srcByteCodeArray);
                    } else {
                        transformMap.put(classBeingRedefined, srcByteCodeArray);
                    }
                    transformCount++;
                    return dumpClassIfNecessary(cr.getClassName(), cw.toByteArray(), srcByteCodeArray);
                }
            }
        } catch (Throwable throwable) {
            DongTaiLog.warn(ErrorCode.get("TRANSFORM_CLASS_FAILED"), internalClassName, throwable);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }

        return null;
    }

    /**
     * 创建ClassWriter for asm
     *
     * @param cr ClassReader
     * @return ClassWriter
     */
    private ClassWriter createClassWriter(final ClassLoader targetClassLoader,
                                          final ClassReader cr) {
        return new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS) {

            /*
             * 注意，为了自动计算帧的大小，有时必须计算两个类共同的父类。
             * 缺省情况下，ClassWriter将会在getCommonSuperClass方法中计算这些，通过在加载这两个类进入虚拟机时，使用反射API来计算。
             * 但是，如果你将要生成的几个类相互之间引用，这将会带来问题，因为引用的类可能还不存在。
             * 在这种情况下，你可以重写getCommonSuperClass方法来解决这个问题。
             *
             * 通过重写 getCommonSuperClass() 方法，更正获取ClassLoader的方式，改成使用指定ClassLoader的方式进行。
             * 规避了原有代码采用Object.class.getClassLoader()的方式
             */
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return AsmUtils.getCommonSuperClass(type1, type2, targetClassLoader);
            }

        };
    }

    /**
     * dump字节码文件到本地，用于DEBUG
     */
    private byte[] dumpClassIfNecessary(String className, byte[] data, byte[] originalData) {
        if (!isDumpClass) {
            return data;
        }
        String javaClassName = className.replace('/', '.');
        String filename;
        String path = properties.getDumpClassPath();
        if (javaClassName.lastIndexOf('.') == -1) {
            filename = javaClassName;
        } else {
            path = path + javaClassName.substring(0, javaClassName.lastIndexOf('.')) + File.separator;
            filename = javaClassName.substring(javaClassName.lastIndexOf('.') + 1);
        }
        try {
            final File enhancedClass = new File(path + filename + ".class");
            final File originalClass = new File(path + filename + "-original.class");
            final File classPath = new File(enhancedClass.getParent());

            if (!classPath.mkdirs() && !classPath.exists()) {
                DongTaiLog.warn(ErrorCode.get("TRANSFORM_CREATE_CLASS_DUMP_DIR_FAILED"), classPath);
                return data;
            }

            writeByteArrayToFile(enhancedClass, data);
            writeByteArrayToFile(originalClass, originalData);
            DongTaiLog.trace("dump class {} to {} success.", className, enhancedClass);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("TRANSFORM_CLASS_DUMP_FAILED"), className, e);
        }

        return data;
    }

    /**
     * 找到需要修改字节码的类
     *
     * @return classes need to retransform
     */
    public Class<?>[] findForRetransform() {
        final Class<?>[] loaded = inst.getAllLoadedClasses();
        final Class<?>[] enhanceClasses = new Class[loaded.length];
        // 获取所有的
        int enhanceClassSize = 0;
        for (Class<?> clazz : loaded) {
            if (clazz == null) {
                continue;
            }
            try {
                if (!configMatcher.canHook(clazz, this.policyManager)) {
                    continue;
                }
                String className = clazz.getName();
                Set<String> diagram = classDiagram.getClassAncestorSet(className);
                if (diagram == null) {
                    diagram = ConcurrentHashMap.newKeySet();
                    Queue<Class<?>> classQueue = new LinkedList<Class<?>>();

                    classQueue.add(clazz);
                    while (classQueue.size() > 0) {
                        Class<?> currentClazz = classQueue.poll();
                        diagram.add(currentClazz.getName());

                        Class<?> superClazz = currentClazz.getSuperclass();
                        if (null != superClazz && superClazz != Object.class) {
                            classQueue.add(superClazz);
                        }
                        Class<?>[] interfaces = currentClazz.getInterfaces();
                        Collections.addAll(classQueue, interfaces);
                    }
                    classDiagram.setClassAncestorSet(className, diagram);
                }
                for (String clazzName : diagram) {
                    if (this.policyManager.isHookClass(clazzName) ||
                            (this.policyManager.getPolicy() != null && this.policyManager.getPolicy().isMatchClass(clazzName))) {
                        enhanceClasses[enhanceClassSize++] = clazz;
                        break;
                    }
                }
            } catch (Throwable cause) {
                // 在这里可能会遇到非常坑爹的模块卸载错误
                // 当一个URLClassLoader被动态关闭之后，但JVM已经加载的类并不知情（因为没有GC）
                // 所以当尝试获取这个类更多详细信息的时候会引起关联类的ClassNotFoundException等未知的错误（取决于底层ClassLoader的实现）
                // 这里没有办法穷举出所有的异常情况，所以catch Throwable来完成异常容灾处理
                // 当解析类出现异常的时候，直接简单粗暴的认为根本没有这个类就好了
                DongTaiLog.trace("remove from findForReTransform, because loading class:" + clazz.getName()
                        + " occur an exception", cause);
            }
        }
        Class<?>[] classes = new Class[enhanceClassSize];
        System.arraycopy(enhanceClasses, 0, classes, 0, enhanceClassSize);
        return classes;
    }

    /**
     * 执行字节码转换
     */
    public void reTransform() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Class<?>[] waitingReTransformClasses = findForRetransform();
        DongTaiLog.debug("find {} classes to reTransform, time: {}", waitingReTransformClasses.length, stopWatch.getTime());
        // fixme: Performance Loss Calculation, 6752 * 50ms = 337600ms, 337s, 6-7min
        for (Class<?> clazz : waitingReTransformClasses) {
            try {
                inst.retransformClasses(clazz);
            } catch (ClassCircularityError e) {
                DongTaiLog.error(ErrorCode.get("RETRANSFORM_CLASS_CIRCULARITY_ERROR"), clazz.getCanonicalName(),
                        e.getCause() != null ? e.getCause().getMessage() : "");
            } catch (InternalError ignored) {
            } catch (Throwable e) {
                DongTaiLog.error(ErrorCode.get("RETRANSFORM_CLASS_FAILED"), clazz.getCanonicalName(), e);
            }
        }
        stopWatch.stop();
        DongTaiLog.debug("finish reTransform, class count: {}, time: {}", getTransformCount(), stopWatch.getTime());
    }

    public static HashMap<Object, byte[]> getTransformMap() {
        return transformMap;
    }

    public IastClassDiagram getClassDiagram() {
        return classDiagram;
    }
}

