package com.secnium.iast.core.util.matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServerClass extends ClassBase {
    private static final String[] CLASSES;

    @Override
    public boolean match(String classname) {
        return StringUtils.startsWithAny(classname, CLASSES);
    }

    static {
        CLASSES = new String[]{
                " org/apache/".substring(1),
        };
    }
}
