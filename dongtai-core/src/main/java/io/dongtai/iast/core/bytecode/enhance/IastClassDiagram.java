package io.dongtai.iast.core.bytecode.enhance;

import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.utils.ThrowableUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.objectweb.asm.ClassReader;

/**
 * 查询给定类的类族
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IastClassDiagram {

    private final Map<String, Set<String>> diagrams;
    private static final Map<String, List<String>> DEFAULT_INTERFACE_LIST_MAP;
    private static final String BASE_CLASS = "java/lang/Object";

    public synchronized void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    private ClassLoader loader;
    private static IastClassDiagram instance;

    public static IastClassDiagram getInstance() {
        if (instance == null) {
            instance = new IastClassDiagram();
        }
        return instance;
    }

    public Set<String> getDiagram(String className) {
        return diagrams.get(className);
    }

    public void setDiagram(String className, Set<String> diagram) {
        diagrams.put(className, diagram);
    }

    private IastClassDiagram() {
        this.diagrams = new ConcurrentHashMap<String, Set<String>>();
    }

    public synchronized void saveAncestors(String className, String superName, String[] interfaces) {
        Set<String> ancestorSet = this.diagrams.get(className);
        ancestorSet = ancestorSet == null ? new HashSet<String>() : ancestorSet;

        ancestorSet.add(className);
        if (!BASE_CLASS.equals(superName)) {
            ancestorSet.add(superName.replace("/", "."));
        }
        for (String interfaceClazzName : interfaces) {
            ancestorSet.add(interfaceClazzName.replace("/", "."));
        }

        this.diagrams.put(className, ancestorSet);
    }

    /**
     * 获取当前类的类族
     *
     * @param className      当前类（待检查的类）
     * @param superClassName 当前类继承的父类
     * @param interfaces     当前类实现的接口列表
     * @return 当前类的类族
     */
    public synchronized Set<String> getAncestors(String className, String superClassName, String[] interfaces) {
        Set<String> ancestors = this.diagrams.get(className);

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

    /**
     * @param className java/lang/Object
     * @param ancestors dirgrams, java.lang.Object Set
     */
    private void addClassToAncestor(String className, Set<String> ancestors) {
        Set<String> set = this.diagrams.get(className.replace("/", "."));
        if (null != set) {
            for (String subClassName : set) {
                if (!ancestors.contains(subClassName)) {
                    ancestors.add(subClassName);
                    addClassToAncestor(subClassName.replace(".", "/"), ancestors);
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
                    Set<String> tempClassMap = diagrams.get(tempClass);
                    if (tempClassMap != null) {
                        ancestors.addAll(tempClassMap);
                    }
                }
                this.diagrams.put(className.replace("/", "."), tempClassFamily);
            }
        }
        List<String> list = DEFAULT_INTERFACE_LIST_MAP.get(className);
        if (null != list) {
            for (String interfaceName : list) {
                if (!ancestors.contains(className)) {
                    ancestors.add(interfaceName);
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
                        ancestors.add(superclass.replace("/", "."));
                        queue.offer(superclass);
                    }
                    for (String tempInterface : interfaces) {
                        ancestors.add(tempInterface.replace("/", "."));
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
     * todo 利用类名查找实现的接口列表、继承的父类
     */
    public static Set<String> getFamilyFromClass(String className) {
        return instance == null ? null : instance.diagrams.get(className);
    }

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
}
