package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.UpdateUtils;
import com.secnium.iast.agent.manager.EngineManager;

/**
 * 监控云端，判断检测引擎是否需要更新
 *
 * @author dongzhiyong@huoxian.cn
 */
public class UpdateMonitor implements IMonitor {
    private final EngineManager engineManager;

    public UpdateMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    /**
     * 访问远程API接口检测是否需要更新引擎
     *
     * @return 布尔值，表示是否需要更新
     */
    private boolean isUpdate() {
        return UpdateUtils.checkForUpdate();
    }

    /**
     * 检测引擎生命周期管理
     * - 安装、启动、暂停、卸载
     */
    @Override
    public void check() {
        if (isUpdate()) {
            boolean status = this.engineManager.stop();
            status = status && this.engineManager.uninstall();
            status = status && this.engineManager.updateEnginePackage();
            status = status && this.engineManager.install();
            status = status && this.engineManager.start();

            if (status) {
                UpdateUtils.setUpdateSuccess();
                System.out.println("[cn.huoxian.dongtai.iast] UpdateMonitor.check DongTai Engine updateEnginePackage success.");
            } else {
                UpdateUtils.setUpdateFailure();
                System.out.println("[cn.huoxian.dongtai.iast] UpdateMonitor.check DongTai Engine updateEnginePackage failure.");
            }
        }
    }
}
