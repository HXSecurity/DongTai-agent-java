package com.secnium.iast.agent.middlewarerecognition;

/**
 * @author dongzhiyong@huoxian.cn
 */
public interface Supplier<R> {
    R get();
}