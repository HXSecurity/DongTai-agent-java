package io.dongtai.iast.agent.monitor;


/**
 * 服务端下发指令枚举
 *
 * @author chenyi
 * @date 2022/3/8
 */
public enum ServerCommandEnum {
    /**
     * 服务端下发指令枚举
     */
    NO_CMD("notcmd","无下发指令"),

    CORE_REGISTER_START("coreRegisterStart","注册启动引擎"),

    CORE_START("coreStart","开启引擎核心"),

    CORE_STOP("coreStop","关闭引擎核心"),

    CORE_UNINSTALL("coreUninstall","卸载引擎核心"),

    CORE_PERFORMANCE_FORCE_OPEN("corePerformanceForceOpen", "强制开启引擎核心性能熔断"),

    CORE_PERFORMANCE_FORCE_CLOSE("corePerformanceForceClose", "强制关闭引擎核心性能熔断"),

    ;
    /**
     * 指令名称
     */
    private final String command;

    /**
     * 描述
     */
    private final String desc;

    ServerCommandEnum(String command, String desc) {
        this.command = command;
        this.desc = desc;
    }

    public static ServerCommandEnum getEnum(String command) {
        if (command != null) {
            for (ServerCommandEnum each : ServerCommandEnum.values()) {
                if (command.equals(each.getCommand())) {
                    return each;
                }
            }
        }
        return null;
    }

    public String getCommand() {
        return command;
    }

    public String getDesc() {
        return desc;
    }
}
