package io.dongtai.iast.core.handler.hookpoint.controller;

/**
 * Hook策略类型-类型值
 *
 * default hook type: 1*
 * company custom hook type: 2*
 *
 * @author dongzhiyong@huoxian.cn
 */

public enum HookType {

    /**
     * HTTP方法hook
     */
    DUBBO("dubbo", 15),
    HTTP("http", 10),
    SOURCE("source", 11),
    PROPAGATOR("propagator", 12),
    SINK("sink", 13),
    SPRINGAPPLICATION("springApplication", 14);


    /**
     * hook类型的值
     */
    int value;
    /**
     * hook类型的名称
     */
    String name;

    HookType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public boolean equals(int target) {
        return this.value == target;
    }

}
