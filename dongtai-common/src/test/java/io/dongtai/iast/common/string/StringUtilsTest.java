package io.dongtai.iast.common.string;

import io.dongtai.iast.common.string.StringUtils;
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

    @Test
    public void formatClassNameToDotDelimiter() {
        String s = StringUtils.formatClassNameToDotDelimiter("com/foo/bar");
        Assert.assertEquals("com.foo.bar", s);
    }

    @Test
    public void formatClassNameToSlashDelimiter() {
        String s = StringUtils.formatClassNameToSlashDelimiter("com.foo.bar");
        Assert.assertEquals("com/foo/bar", s);
    }

}