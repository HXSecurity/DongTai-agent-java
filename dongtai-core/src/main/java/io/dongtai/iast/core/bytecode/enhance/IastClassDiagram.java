package io.dongtai.iast.core.bytecode.enhance;

import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.objectweb.asm.ClassReader;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static io.dongtai.iast.common.string.StringUtils.formatClassNameToDotDelimiter;
import static io.dongtai.iast.common.string.StringUtils.formatClassNameToSlashDelimiter;

/**
 * 用于存储类名称到祖先类的映射关系：类的名字 --> Set<祖先类的名称集合>
 * <p>
 * 因为策略中的Hook范围有个继承关系，这个类存储的继承数据在Hook的时候提供参考依据
 * <p>
 * 这里存储的是继承的叠加态，暂不考虑减少的情况，职责是尽可能的发现尽可能多的继承关系，然后累计去重存储在这里供后面查询使用
 *
 * @author dongzhiyong@huoxian.cn
 * @author refactored by CC11001100 at v1.13.2
 */
public class IastClassDiagram {

    // 最顶端的基类要被排除掉不参与这些人间的琐事
    private static final String BASE_CLASS = "java.lang.Object";

    // 有一些继承关系是写死的？可能是读取不到？不清楚是什么情况了...反正从第一版就有，根本不敢动，原样带着...
    private static final Map<String, List<String>> DEFAULT_INTERFACE_LIST_MAP;

    static {
        DEFAULT_INTERFACE_LIST_MAP = new HashMap<String, List<String>>();
        DEFAULT_INTERFACE_LIST_MAP.put(" org.apache.jasper.runtime.HttpJspBase".substring(1),
                Collections.singletonList(" javax.servlet.jsp.JspPage".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" javax.servlet.http.HttpServletResponse".substring(1),
                Collections.singletonList(" javax.servlet.ServletResponse".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" javax.servlet.http.HttpServletRequest".substring(1),
                Collections.singletonList(" javax.servlet.ServletRequest".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" weblogic.servlet.internal.ServletRequestImpl".substring(1),
                Collections.singletonList(" javax.servlet.ServletRequest".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" weblogic.servlet.jsp.JspBase".substring(1),
                Collections.singletonList(" javax.servlet.http.HttpServlet".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" com.mysql.jdbc.Statement".substring(1),
                Collections.singletonList(" java.sql.Statement".substring(1)));
    }

    // 存储类名到祖先类的映射关系
    // <className, Set<String>>
    private final Map<String, Set<String>> diagrams;

    // DCL单例
    private static volatile IastClassDiagram instance;

    private IastClassDiagram() {
        this.diagrams = new ConcurrentHashMap<String, Set<String>>();
    }

    /**
     * 单例模式初始化
     *
     * @return
     */
    public static IastClassDiagram getInstance() {
        // DCL
        if (instance == null) {
            synchronized (IastClassDiagram.class) {
                if (instance == null) {
                    instance = new IastClassDiagram();
                }
            }
        }
        return instance;
    }

    /**
     * 获取类的所有祖先
     *
     * @param className .分隔的全路径类名，比如 com.foo.Bar
     * @return
     */
    public Set<String> getClassAncestorSet(String className) {
        return this.diagrams.get(className);
    }

    /**
     * 设置类的祖先，覆盖式替换更新
     *
     * @param className        .分隔的全路径类名，比如 com.foo.Bar
     * @param classAncestorSet
     */
    public void setClassAncestorSet(String className, Set<String> classAncestorSet) {
        // 确保放入的是一个并发安全的set，如果不是的话就替换为一个并发安全的set，这样后面直接对set操作才会没问题
        if (!isConcurrentSafeSet(classAncestorSet)) {
            Set<String> concurrentSafeSet = ConcurrentHashMap.newKeySet();
            concurrentSafeSet.addAll(classAncestorSet);
            classAncestorSet = concurrentSafeSet;
        }
        this.diagrams.put(className, classAncestorSet);
    }

    /**
     * 判断set是否是并发安全的
     *
     * @param set
     */
    private boolean isConcurrentSafeSet(Set<String> set) {
        if (set == null) {
            return false;
        }
        return set.getClass().getName().startsWith("java.util.concurrent.");
    }

    /**
     * 更新给定类的祖先类集合
     *
     * @param classLoader  类所属的加载器，用于读取jar资源
     * @param classContext 要解析的类的上下文
     * @return 返回更新后的祖先类集合，不需要的话可以忽略返回值
     */
    public Set<String> updateAncestorsByClassContext(ClassLoader classLoader, ClassContext classContext) {

        // 如果没有传递ClassLoader的话则使用当前类的ClassLoader，但感觉这可能会有点问题？祖传逻辑，不敢动...
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        // 以.分隔的全路径类名
        String className = classContext.getClassName();
        Set<String> ancestorSet = this.diagrams.computeIfAbsent(className, k -> ConcurrentHashMap.newKeySet());

        // 认为当前类的直接继承关系是已经解析好的不再重复解析，所以当前类是直接加入到结果集而不是加入到队列中
        // 当前类的继承关系会从ClassContext上读取，这样避免了一次解析
        ancestorSet.add(className);

        // 如果有默认继承关系的话则把这个继承关系也处理一下，但是要注意这里是直接加入到结果集中
        if (DEFAULT_INTERFACE_LIST_MAP.containsKey(className)) {
            ancestorSet.addAll(DEFAULT_INTERFACE_LIST_MAP.get(className));
        }

        // 然后就是构造队列，一直递归往上找了，收集要查找的类都有哪些
        Queue<String> queue = new LinkedList<String>();

        // 父类要参与查找，将其加入到队列
        String superClassName = formatClassNameToDotDelimiter(classContext.getSuperClassName());
        if (isValidSuperClass(superClassName)) {
            queue.add(superClassName);
        }

        // 实现的接口要参与查找，将其加入到队列
        String[] implementInterfaces = classContext.getInterfaces();
        if (implementInterfaces != null && implementInterfaces.length != 0) {
            for (String implementInterface : implementInterfaces) {
                queue.add(formatClassNameToDotDelimiter(implementInterface));
            }
        }

        // 然后就是递归向上查找
        while (!queue.isEmpty()) {

            String currentClassName = queue.poll();

            // 如果已经处理过这个继承关系了则不再重复处理，虽然理论上应该不会产生环，但还是把这条路给堵死
            if (ancestorSet.contains(currentClassName)) {
                continue;
            }

            // 把自己加入到祖先集合中
            ancestorSet.add(currentClassName);

            // 如果之前已经处理过这个类，则从缓存中读取之前已经处理过的结果
            if (this.diagrams.containsKey(currentClassName)) {
                this.diagrams.get(currentClassName).forEach(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        queue.offer(s);
                    }
                });
            } else {
                // 缓存中没有，则从文件中读取
                try {
                    String classpath = formatClassNameToSlashDelimiter(currentClassName) + ".class";
                    InputStream inputStream = classLoader.getResourceAsStream(classpath);
                    if (inputStream == null) {
                        // TODO 2023-8-28 16:14:03 查找不到的情况怎么办
                        continue;
                    }
                    ClassReader cr = new ClassReader(inputStream);
                    inputStream.close();

                    // 把父类加入到队列
                    superClassName = formatClassNameToDotDelimiter(cr.getSuperName());
                    if (isValidSuperClass(superClassName)) {
                        queue.offer(superClassName);
                    }

                    // 合并父接口
                    implementInterfaces = cr.getInterfaces();
                    for (String interfaceName : implementInterfaces) {
                        queue.offer(formatClassNameToDotDelimiter(interfaceName));
                    }

                    // 如果有默认继承关系的话则把这个继承关系也处理一下，但是要注意，这里是直接加入到结果集而不是加入到队列
                    // 这是因为目前的策略用到的类的最高级别不会超过DEFAULT_INTERFACE_LIST_MAP给定的父类，所以没必要再去往上继续寻找
                    if (DEFAULT_INTERFACE_LIST_MAP.containsKey(className)) {
                        ancestorSet.addAll(DEFAULT_INTERFACE_LIST_MAP.get(className));
                    }

                } catch (Throwable e) {
                    DongTaiLog.warn(ErrorCode.get("CLASS_DIAGRAM_SCAN_JAR_ANCESTOR_FAILED"), e);
                }
            }
        }

        return ancestorSet;
    }

    /**
     * 判断类是否是可用的父类，是为了排除形如 java/lang/Object 的格式
     *
     * @param superclass
     * @return
     */
    private static boolean isValidSuperClass(String superclass) {
        return superclass != null && !BASE_CLASS.equals(superclass);
    }

}
