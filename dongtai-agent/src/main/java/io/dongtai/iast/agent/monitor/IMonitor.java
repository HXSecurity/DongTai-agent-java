package io.dongtai.iast.agent.monitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public interface IMonitor extends Runnable {
    void check();
    String getName();
}
