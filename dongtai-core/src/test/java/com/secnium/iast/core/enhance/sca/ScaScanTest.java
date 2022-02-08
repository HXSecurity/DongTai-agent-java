package com.secnium.iast.core.enhance.sca;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;

public class ScaScanTest {

    @Test
    public void scan() throws MalformedURLException {
        String[] packagePaths = new String[]{
        };

        for (String packagePath : packagePaths) {
            ScaScanner.scanForSCA(new URL(packagePath).getFile(), "");
        }
    }
}