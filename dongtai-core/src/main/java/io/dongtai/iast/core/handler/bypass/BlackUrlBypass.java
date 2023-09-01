package io.dongtai.iast.core.handler.bypass;

import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;

public class BlackUrlBypass {

    private static final BooleanThreadLocal isBlackUrl = new BooleanThreadLocal(false);

    public static Boolean isBlackUrl() {
        return isBlackUrl.get();
    }

    public static void setIsBlackUrl(Boolean isBlackUrl) {
        BlackUrlBypass.isBlackUrl.set(isBlackUrl);
    }

    public static String getHeaderKey() {
        return "dt-collect-skip";
    }
}
