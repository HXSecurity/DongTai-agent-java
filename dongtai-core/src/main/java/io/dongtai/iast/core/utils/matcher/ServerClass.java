package io.dongtai.iast.core.utils.matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServerClass extends AbstractMatcher {
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
