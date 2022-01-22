package com.secnium.iast.core.util.matcher;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractMatcher {

    /**
     * 检查目标类与当前实现是否匹配
     *
     * @param classname
     * @return
     */
    public abstract boolean match(String classname);
}
