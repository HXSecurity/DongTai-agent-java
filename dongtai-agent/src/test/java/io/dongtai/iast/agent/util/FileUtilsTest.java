package io.dongtai.iast.agent.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FileUtilsTest {
    @Test
    public void testGetResourceToFile() {
        // 测试getResourceToFile方法是否能正确地将资源文件复制到指定的文件路径

        //获取临时文件路径
        String tempDirectoryPath = org.apache.commons.io.FileUtils.getTempDirectoryPath();

        String resourceName = "iast.properties.example"; // 假设存在名为test_resource.txt的资源文件
        String fileName = tempDirectoryPath + "test.example"; // 替换为实际的目标文件路径

        boolean result;
        try {
            result = FileUtils.getResourceToFile(resourceName, fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue(result); // 验证复制操作是否成功

        // 验证目标文件是否存在
        java.io.File targetFile = new java.io.File(fileName);
        Assert.assertTrue(targetFile.exists());

        // 清理测试产生的文件
        targetFile.delete();
    }
}
