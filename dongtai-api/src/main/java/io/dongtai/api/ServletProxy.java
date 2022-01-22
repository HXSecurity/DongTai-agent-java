package io.dongtai.api;

import io.dongtai.api.jakarta.JakartaRequestWrapper;
import io.dongtai.api.jakarta.JakartaResponseWrapper;
import io.dongtai.api.servlet2.ServletRequestWrapper;
import io.dongtai.api.servlet2.ServletResponseWrapper;

/**
 * @author owefsad
 * @since 1.3.1
 */
public class ServletProxy {

    public static Object cloneRequest(Object request, boolean isJakarta) {
        if (isJakarta) {
            return new JakartaRequestWrapper((jakarta.servlet.http.HttpServletRequest) request);
        } else {
            return new ServletRequestWrapper((javax.servlet.http.HttpServletRequest) request);
        }
    }

    public static Object cloneResponse(Object response, boolean isJakarta) {
        if (isJakarta) {
            return new JakartaResponseWrapper((jakarta.servlet.http.HttpServletResponse) response);
        } else {
            return new ServletResponseWrapper((javax.servlet.http.HttpServletResponse) response);
        }
    }

}
