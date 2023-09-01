package io.dongtai.iast.common.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author CC11001100
 */
public class ExceptionUtilTest {

    @Test
    public void getPrintStackTraceString() {
        Exception e = new Exception();
        String printStackTraceString = ExceptionUtil.getPrintStackTraceString(e);
        Assert.assertNotNull(printStackTraceString);
    }

}