package com.secnium.iast.agent;

import org.junit.Test;

public class UpdateUtilsTest {
    @Test
    public void checkForUpdate() {
        boolean status = UpdateUtils.needUpdate();
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
