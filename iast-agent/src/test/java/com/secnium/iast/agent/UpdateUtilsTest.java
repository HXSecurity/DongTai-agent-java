package com.secnium.iast.agent;

import org.junit.Test;

public class UpdateUtilsTest {
    @Test
    public void checkForUpdate() {
        boolean status = UpdateUtils.checkForUpdate();
        System.out.println("updateEnginePackage status:" + status);
    }

    @Test
    public void setUpdateSuccess() {
        UpdateUtils.setUpdateSuccess();
    }

    @Test
    public void setUpdateFailure() {
        UpdateUtils.setUpdateFailure();
    }
}
