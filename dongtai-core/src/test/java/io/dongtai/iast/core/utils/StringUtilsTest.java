package io.dongtai.iast.core.utils;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void normalize() {
        int maxLength = 1024;
        String str;
        String nStr;
        str = new String(new char[1000]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("length", 1000, nStr.length());
        str = new String(new char[10000]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("length overflow", maxLength, nStr.length());

        maxLength = 7;
        str = new String(new char[10]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("max len 7", "aa...aa", nStr);
        maxLength = 6;
        str = new String(new char[10]).replace("\0", "a");
        nStr = StringUtils.normalize(str, maxLength);
        Assert.assertEquals("max len 6", "aa...a", nStr);
    }
}