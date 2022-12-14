package io.dongtai.iast.agent.monitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public interface IMonitor extends Runnable {
    /**
     * 监控器检查
     */
    void check();

    /**
     * 获取监控器线程名称
     *
     * @return {@link String} 监控器名称
     */
    String getName();
}
