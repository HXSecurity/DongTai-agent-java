package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.utils.StringUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.json.JSONArray;

import java.util.*;

public class PolicyManager {
    private Policy policy;
    /**
     * hook class names for no policy
     */
    private static final Set<String> HOOK_CLASS_NAMES = new HashSet<String>(Arrays.asList(
            " javax.servlet.Filter".substring(1),
            " javax.servlet.FilterChain".substring(1),
            " javax.servlet.http.HttpServlet".substring(1),
            " jakarta.servlet.http.HttpServlet".substring(1),
            " javax.faces.webapp.FacesServlet".substring(1),
            " javax.servlet.jsp.JspPage".substring(1),
            " org.apache.jasper.runtime.HttpJspBase".substring(1),
            " org.springframework.web.servlet.FrameworkServlet".substring(1),
            " javax.servlet.http.Cookie".substring(1),
            " org/springframework/web/servlet/mvc/annotation/AnnotationMethodHandlerAdapter$ServletHandlerMethodInvoker".substring(1),
            " feign.SynchronousMethodHandler.invoke".substring(1)
    ));
    private static final Set<String> HOOK_CLASS_SUFFIX_NAMES = new HashSet<String>(Collections.singletonList(
            ".dubbo.monitor.support.MonitorFilter"
    ));

    public Policy getPolicy() {
        return this.policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public void loadPolicy(String policyPath) {
        try {
            JSONArray policyConfig;
            if (StringUtils.isEmpty(policyPath)) {
                policyConfig = PolicyBuilder.fetchFromServer();
            } else {
                policyConfig = PolicyBuilder.fetchFromFile(policyPath);
            }
            this.policy = PolicyBuilder.build(policyConfig);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.POLICY_LOAD_FAILED, e);
        }
    }

    public static boolean isHookClass(String className) {
        return HOOK_CLASS_NAMES.contains(className) || hookBySuffix(className);
    }

    private static boolean hookBySuffix(String classname) {
        for (String suffix : HOOK_CLASS_SUFFIX_NAMES) {
            if (classname.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
