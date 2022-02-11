package io.dongtai.iast.core.utils.threadlocal;

import java.util.HashSet;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastTaintHashCodes extends ThreadLocal<HashSet<Integer>> {
    @Override
    protected HashSet<Integer> initialValue() {
        return null;
    }


}
