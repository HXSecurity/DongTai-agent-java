package com.secnium.iast.core.enhance;

import com.secnium.iast.core.enhance.sca.ScaScanner;
import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.ThrowableUtils;
import org.json.JSONObject;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 查询给定类的类族
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IastClassAncestorQuery {

    private final Map<String, Set<String>> classAncestorMap;
    private static final Map<String, List<String>> DEFAULT_INTERFACE_LIST_MAP;
    private static final String BASE_CLASS = "java/lang/Object";
    private final HashSet<String> scannedClassSet = new HashSet<String>();

    public synchronized void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    private ClassLoader loader;
    private static IastClassAncestorQuery instance;

    public static IastClassAncestorQuery getInstance() {
        if (instance == null) {
            instance = new IastClassAncestorQuery();
        }
        return instance;
    }

    private IastClassAncestorQuery() {
        this.classAncestorMap = new ConcurrentHashMap<String, Set<String>>();
    }

    public synchronized void saveAncestors(String className, String superName, String[] interfaces) {
        Set<String> ancestorSet = this.classAncestorMap.get(className);
        ancestorSet = ancestorSet == null ? new HashSet<String>() : ancestorSet;

        if (!BASE_CLASS.equals(superName)) {
            ancestorSet.add(superName);
        }

        Collections.addAll(ancestorSet, interfaces);

        this.classAncestorMap.put(className, ancestorSet);
    }

    /**
     * 获取当前类的类族
     *
     * @param className      当前类（待检查的类）
     * @param superClassName 当前类继承的父类
     * @param interfaces     当前类实现的接口列表
     * @return 当前类的类族
     */
    public synchronized HashSet<String> getAncestors(String className, String superClassName, String[] interfaces) {
        HashSet<String> ancestors = (HashSet<String>) this.classAncestorMap.get(className);

        if (!isNullOrEmpty(superClassName) && !BASE_CLASS.equals(superClassName)) {
            addClassToAncestor(superClassName, ancestors);
        }

        List<String> interfaceList = DEFAULT_INTERFACE_LIST_MAP.get(className);
        if (interfaceList != null) {
            ancestors.addAll(interfaceList);
        }

        for (String anInterface : interfaces) {
            addClassToAncestor(anInterface, ancestors);
        }

        return ancestors;

    }

    private void addClassToAncestor(String className, Set<String> ancestors) {
        Set<String> set = this.classAncestorMap.get(className);
        if (null != set) {
            for (String subClassName : set) {
                if (!ancestors.contains(subClassName)) {
                    ancestors.add(className);
                    addClassToAncestor(subClassName, ancestors);
                }
            }
        } else {
            Set<String> tempClassFamily = new HashSet<String>();
            scanJarForAncestor(className, tempClassFamily);
            if (!tempClassFamily.isEmpty()) {
                for (String tempClass : tempClassFamily) {
                    ancestors.add(tempClass);
                    List<String> tempDefaultMap = DEFAULT_INTERFACE_LIST_MAP.get(tempClass);
                    if (tempDefaultMap != null) {
                        ancestors.addAll(tempDefaultMap);
                    }
                    Set<String> tempClassMap = classAncestorMap.get(tempClass);
                    if (tempClassMap != null) {
                        ancestors.addAll(tempClassMap);
                    }
                }
                this.classAncestorMap.put(className, tempClassFamily);
            }
        }
        List<String> list = DEFAULT_INTERFACE_LIST_MAP.get(className);
        if (null != list) {
            for (String interfaceName : list) {
                if (!ancestors.contains(className)) {
                    ancestors.add(className);
                    addClassToAncestor(interfaceName, ancestors);
                }
            }
        }
    }

    /**
     * 从classloader中查找父类，将当前类的父类收集足够完整，避免丢失hook点，该方法未做持续查找，缺少深度
     *
     * @param className 当前类名
     * @param ancestors 当前类的祖先 类/接口 集合
     */
    private void scanJarForAncestor(String className, Set<String> ancestors) {
        if (null == this.loader) {
            this.loader = this.getClass().getClassLoader();
        }
        if (null == this.loader) {
            return;
        }

        Queue<String> queue = new LinkedList<String>();
        queue.offer(className);

        while (!queue.isEmpty()) {
            String currentClass = queue.poll();
            try {
                InputStream inputStream = this.loader.getResourceAsStream(currentClass + ".class");
                if (inputStream != null) {
                    ClassReader cr = new ClassReader(inputStream);
                    inputStream.close();
                    String[] interfaces = cr.getInterfaces();
                    String superclass = cr.getSuperName();
                    if (!(BASE_CLASS.equals(superclass) || null == superclass)) {
                        ancestors.add(superclass);
                        queue.offer(superclass);
                    }
                    ancestors.addAll(Arrays.asList(interfaces));
                    for (String tempInterface : interfaces) {
                        queue.offer(tempInterface);
                    }
                }
            } catch (Exception e) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "scan class for family");
                jsonObject.put("className", currentClass);
                jsonObject.put("msg", ThrowableUtils.getStackTrace(e));
                ErrorLogReport.sendErrorLog(jsonObject.toString());
            }
        }
    }

    public static boolean isNullOrEmpty(String className) {
        return null == className || className.isEmpty();
    }

    /**
     * todo 修改为异步扫描
     *
     * @param codeSource
     */
    public void scanCodeSource(CodeSource codeSource) {
        URL url = codeSource.getLocation();
        if (url != null) {
            String jarPackageFilePath = url.getFile();
            File jarPackageFile = new File(jarPackageFilePath);
            String packagePath = jarPackageFile.getParent();
            if (jarPackageFilePath.startsWith("file:") && jarPackageFilePath.endsWith(".jar!/") && jarPackageFilePath.contains("BOOT-INF") && !scannedClassSet.contains(packagePath)) {
                scannedClassSet.add(packagePath);
                jarPackageFilePath = jarPackageFilePath.replace("file:", "");
                jarPackageFilePath = jarPackageFilePath.substring(0, jarPackageFilePath.indexOf("!/"));
                ScaScanner.scanWithJarPackage(jarPackageFilePath);
            } else if (jarPackageFilePath.endsWith(".jar") && jarPackageFilePath.contains("WEB-INF") && !scannedClassSet.contains(packagePath)) {
                scannedClassSet.add(packagePath);
                File packagePathFile = new File(packagePath);
                File[] packagePathFiles = packagePathFile.listFiles();
                for (File tempPackagePathFile : packagePathFiles != null ? packagePathFiles : new File[0]) {
                    ScaScanner.scan(tempPackagePathFile);
                }
            }else if (jarPackageFilePath.endsWith(".jar") && jarPackageFilePath.contains("repository") && !scannedClassSet.contains(jarPackageFilePath)){
                scannedClassSet.add(jarPackageFilePath);
                ScaScanner.scan(jarPackageFile);
            }
        }
    }

    /**
     * todo 利用类名查找实现的接口列表、继承的父类
     */
    public static Set<String> getFamilyFromClass(String className) {
        return instance == null ? null : instance.classAncestorMap.get(className);
    }

    static {
        DEFAULT_INTERFACE_LIST_MAP = new HashMap();
        DEFAULT_INTERFACE_LIST_MAP.put(" org/apache/jasper/runtime/HttpJspBase".substring(1), Collections.singletonList(" javax/servlet/jsp/JspPage".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" javax/servlet/http/HttpServletResponse".substring(1), Collections.singletonList(" javax/servlet/ServletResponse".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" javax/servlet/http/HttpServletRequest".substring(1), Collections.singletonList(" javax/servlet/ServletRequest".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" weblogic/servlet/internal/ServletRequestImpl".substring(1), Collections.singletonList(" javax/servlet/ServletRequest".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" weblogic/servlet/jsp/JspBase".substring(1), Collections.singletonList(" javax/servlet/http/HttpServlet".substring(1)));
        DEFAULT_INTERFACE_LIST_MAP.put(" com/mysql/jdbc/Statement".substring(1), Collections.singletonList(" java/sql/Statement".substring(1)));
    }
}
