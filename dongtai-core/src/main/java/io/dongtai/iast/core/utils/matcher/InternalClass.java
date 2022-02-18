package io.dongtai.iast.core.utils.matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class InternalClass extends AbstractMatcher {
    private static final String[] classes;

    @Override
    public boolean match(String classname) {
        return StringUtils.startsWithAny(classname, classes);
    }

    static {
        classes = new String[]{
                "apple/",
                "com/intellij/",
                "com/sun/beans/",
                "com/sun/jmx/",
                "com/sun/jndi/",
                "com/sun/management/",
                "com/sun/naming/",
                "com/sun/net/",
                "com/sun/org/",
                "com/sun/proxy/",
                "com/sun/security/",
                "com/sun/xml/",
                "java/beans/",
                "java/io/",
                "java/lang/",
                "java/math/",
                "java/net/",
                "java/nio/",
                "java/rmi/",
                "java/security/",
                "java/sql/",
                "java/text/",
                "java/time/",
                "java/util/",
                "javax/annotation/",
                "javax/crypto/",
                "javax/management/",
                "javax/naming/",
                "javax/net/",
                "javax/rmi/",
                "javax/security/",
                "javax/sql/",
                "javax/xml/",
                "jdk/",
                "org/ietf/jgss/",
                "org/jcp/xml/",
                "org/omg/CORBA/",
                "org/omg/stub/",
                "org/w3c/dom/",
                "org/xml/sax/",
                "sun/",
        };
    }
}
