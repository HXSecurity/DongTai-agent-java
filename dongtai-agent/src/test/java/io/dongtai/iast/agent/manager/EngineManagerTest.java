package io.dongtai.iast.agent.manager;

import io.dongtai.iast.agent.IastProperties;
import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ManagementFactory;

public class EngineManagerTest {

    @Test
    public void extractPIDTest(){
        //初始化临时文件
        IastProperties.initTmpDir();
        //获取PID
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();

        //Java获取的runtimeName格式为 PID@虚拟机唯一标识 提取PID
        String pid = EngineManager.extractPID(runtimeName);
        Assert.assertTrue(pid.matches("\\d+"));
    }
}
