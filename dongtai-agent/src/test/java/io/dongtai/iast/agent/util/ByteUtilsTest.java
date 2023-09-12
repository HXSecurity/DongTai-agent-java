package io.dongtai.iast.agent.util;

import org.junit.Assert;
import org.junit.Test;

public class ByteUtilsTest {
    @Test
    public void testFormatByteSize() {
        // 测试formatByteSize方法是否返回正确的格式化字符串

        // 测试不足1KB的情况
        Assert.assertEquals("0B", ByteUtils.formatByteSize(0));
        Assert.assertEquals("1023B", ByteUtils.formatByteSize(1023));

        // 测试不足1MB的情况
        Assert.assertEquals("1KB", ByteUtils.formatByteSize(1024));
        Assert. assertEquals("1.5KB", ByteUtils.formatByteSize(1536));
        Assert.assertEquals("1023.5KB", ByteUtils.formatByteSize(1023 * 1024 + 512));

        // 测试不足1GB的情况
        Assert.assertEquals("1MB", ByteUtils.formatByteSize(1024 * 1024));
        Assert.assertEquals("1.5MB", ByteUtils.formatByteSize(1536 * 1024));
        Assert.assertEquals("1023.5MB", ByteUtils.formatByteSize(1023 * 1024 * 1024 + 512 * 1024));

        // 测试不足1TB的情况
        Assert.assertEquals("1GB", ByteUtils.formatByteSize(1024 * 1024 * 1024));
        Assert.assertEquals("1.5GB", ByteUtils.formatByteSize(1536 * 1024 * 1024));
        Assert.assertEquals("1023.5GB", ByteUtils.formatByteSize(1023L * 1024 * 1024 * 1024 + 512 * 1024 * 1024));

        // 测试不足1PB的情况
        Assert.assertEquals("1TB", ByteUtils.formatByteSize(1024L * 1024 * 1024 * 1024));
        Assert.assertEquals("1.5TB", ByteUtils.formatByteSize(1536L * 1024 * 1024 * 1024));
        Assert.assertEquals("1023.5TB", ByteUtils.formatByteSize(1023L * 1024 * 1024 * 1024 * 1024 + 512L * 1024 * 1024 * 1024));

        // 测试超过1PB的情况
        Assert.assertEquals(">PB", ByteUtils.formatByteSize(Long.MAX_VALUE));
    }
}
