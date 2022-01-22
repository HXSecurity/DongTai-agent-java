package com.secnium.iast.core.handler.controller;

/**
 * Hook策略类型-类型值
 *
 * @author dongzhiyong@huoxian.cn
 */

public enum HookType {

    /**
     * HTTP方法hook
     */
    DUBBO("dubbo", 5),
    HTTP("http", 0),
    SOURCE("source", 1),
    PROPAGATOR("propagator", 2),
    SINK("sink", 3),
    SPRINGAPPLICATION("springApplication", 4);


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
