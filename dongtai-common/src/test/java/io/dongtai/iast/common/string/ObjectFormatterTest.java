package io.dongtai.iast.common.string;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author CC11001100
 */
public class ObjectFormatterTest {

    @Test
    public void formatObject() {

        // 普通的字符串
        ObjectFormatResult r = ObjectFormatter.formatObject("CC11001100", 1024);
        Assert.assertNotNull(r);
        Assert.assertEquals(10, r.originalLength);
        Assert.assertEquals("CC11001100", r.objectFormatString);

        // 超过长度限制的字符串
        r = ObjectFormatter.formatObject("Dongtai IAST is an open-source Interactive Application Security Testing (IAST) tool that enables real-time detection of common vulnerabilities in Java applications and third-party components through passive instrumentation. It is particularly suitable for use in the testing phase of the development pipeline.", 100);
        Assert.assertNotNull(r);
        Assert.assertEquals(309, r.originalLength);
        Assert.assertEquals("Dongtai IAST is an open-source Interactive Applic...n the testing phase of the development pipeline.", r.objectFormatString);

        // int
        r = ObjectFormatter.formatObject(10086, 100);
        Assert.assertNotNull(r);
        Assert.assertEquals(5, r.originalLength);
        Assert.assertEquals("10086", r.objectFormatString);

        // null
        r = ObjectFormatter.formatObject(null, 100);
        Assert.assertNotNull(r);
        Assert.assertEquals(0, r.originalLength);
        Assert.assertNull(r.objectFormatString);

        // 数组，加起来长度超了
        r = ObjectFormatter.formatObject(new String[]{
                "foo",
                "bar",
                "blablablablablablablablablablablabla"
        }, 10);
        Assert.assertNotNull(r);
        Assert.assertEquals(42, r.originalLength);
        Assert.assertEquals("foob...bla", r.objectFormatString);

        // 数组，第一个长度超了
        r = ObjectFormatter.formatObject(new String[]{
                "foofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoo",
                "bar",
                "blabla"
        }, 10);
        Assert.assertNotNull(r);
        Assert.assertEquals(117, r.originalLength);
        Assert.assertEquals("foof...bla", r.objectFormatString);

        // 数组，第一个长度没超，加起来超了
        r = ObjectFormatter.formatObject(new String[]{
                "foofoo",
                "barbarbarbarbar",
                "blabla"
        }, 10);
        Assert.assertNotNull(r);
        Assert.assertEquals(27, r.originalLength);
        Assert.assertEquals("foof...bla", r.objectFormatString);

        // 数组，第一个长度没超，加起来也没超
        r = ObjectFormatter.formatObject(new String[]{
                "foo",
                "bar",
                "bla"
        }, 10);
        Assert.assertNotNull(r);
        Assert.assertEquals(9, r.originalLength);
        Assert.assertEquals("foobarbla", r.objectFormatString);

        // 对象数组
        r = ObjectFormatter.formatObject(new Object[]{
                new Object(),
                new Object(),
                new Object(),
                new Object()
        }, 40);
        Assert.assertNotNull(r);
        Assert.assertTrue(r.originalLength > 0);
        Assert.assertNotNull(r.objectFormatString);

        // null数组
        r = ObjectFormatter.formatObject(new Object[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        }, 40);
        Assert.assertNotNull(r);
        Assert.assertEquals(0, r.originalLength);
        Assert.assertEquals("", r.objectFormatString);

        // Integer数组
        r = ObjectFormatter.formatObject(new Integer[]{
                1,
                2,
                3,
                4,
                5
        }, 40);
        Assert.assertNotNull(r);
        Assert.assertEquals(5, r.originalLength);
        Assert.assertEquals("12345", r.objectFormatString);

        // int数组
        r = ObjectFormatter.formatObject(new int[]{
                1,
                2,
                3,
                4,
                5
        }, 40);
        Assert.assertNotNull(r);
        Assert.assertEquals(11, r.originalLength);
        Assert.assertNotNull(r.objectFormatString);

        // 数组中偶尔有null的
        r = ObjectFormatter.formatObject(new Integer[]{
                1,
                null,
                3,
                null,
                5
        }, 40);
        Assert.assertNotNull(r);
        Assert.assertEquals(3, r.originalLength);
        Assert.assertEquals("135", r.objectFormatString);

    }

}