package io.dongtai.api;

import io.dongtai.api.servlet2.ServletRequestWrapper;
import io.dongtai.api.servlet2.ServletResponseWrapper;

/**
 * @author owefsad
 * @since 1.3.1
 */
public class ServletProxy {

    public static Object cloneRequest(Object request, boolean isJakarta) {
        return new ServletRequestWrapper((javax.servlet.http.HttpServletRequest) request);
    }

    public static Object cloneResponse(Object response, boolean isJakarta) {
        return new ServletResponseWrapper((javax.servlet.http.HttpServletResponse) response);
    }

}
