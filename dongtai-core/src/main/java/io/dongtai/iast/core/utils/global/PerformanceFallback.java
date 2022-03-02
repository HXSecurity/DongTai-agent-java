package io.dongtai.iast.core.utils.global;

import io.dongtai.log.DongTaiLog;
import lombok.AllArgsConstructor;

/**
 * 性能熔断开关（测试用）
 *
 * @author liyuan40
 * @date 2022/3/2 14:14
 */
@AllArgsConstructor
public class PerformanceFallback {

    private boolean defaultValue;

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        DongTaiLog.info("Performance Rollback changed from " + this.defaultValue + " to " + defaultValue);
        this.defaultValue = defaultValue;
    }
}
