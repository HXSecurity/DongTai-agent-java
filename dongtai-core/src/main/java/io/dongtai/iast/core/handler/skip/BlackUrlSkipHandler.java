package io.dongtai.iast.core.handler.skip;

import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;

public class BlackUrlSkipHandler {

    private static BooleanThreadLocal isBlackUrl = new BooleanThreadLocal(false);

    public static Boolean isBlackUrl() {
        return isBlackUrl.get();
    }

    public static void setIsBlackUrl(Boolean isBlackUrl) {
        BlackUrlSkipHandler.isBlackUrl.set(isBlackUrl);
    }

    public static String getHeaderKey() {
        return "dt-collect-skip";
    }
}
