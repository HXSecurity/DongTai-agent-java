package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;

import java.util.*;

/**
 * Policy tags
 * TODO: parse tags/untags from policy
 */
public class PolicyTag {
    public static final Map<String, List<TaintTag[]>> TAGS = new HashMap<String, List<TaintTag[]>>() {{
        String sign;

        sign = "javax.servlet.http.HttpServletRequest.getReader()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getQueryString()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE, TaintTag.XSS_ENCODED}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getParts()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getPart(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getParameterValues(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getParameterNames()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getParameterMap()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getInputStream()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getHeaders(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        // sign = "javax.servlet.http.HttpServletRequest.getHeaderNames()";
        // put(sign, Arrays.asList(new TaintTag[0], new TaintTag[0]));
        sign = "javax.servlet.http.HttpServletRequest.getHeader(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        // sign = "javax.servlet.http.HttpServletRequest.getCookies()";
        // put(sign, Arrays.asList(new TaintTag[0], new TaintTag[0]));

        sign = "jakarta.servlet.http.HttpServletRequest.getQueryString()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE, TaintTag.XSS_ENCODED}, new TaintTag[0]));
        sign = "jakarta.servlet.http.HttpServletRequest.getParts()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "jakarta.servlet.http.HttpServletRequest.getPart(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "jakarta.servlet.http.HttpServletRequest.getHeaders(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        // sign = "jakarta.servlet.http.HttpServletRequest.getHeaderNames()";
        // put(sign, Arrays.asList(new TaintTag[0], new TaintTag[0]));
        sign = "jakarta.servlet.http.HttpServletRequest.getHeader(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        // sign = "jakarta.servlet.http.HttpServletRequest.getCookies()";
        // put(sign, Arrays.asList(new TaintTag[0], new TaintTag[0]));

        sign = "javax.servlet.ServletRequest.getReader()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.ServletRequest.getParameterValues(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.ServletRequest.getParameterNames()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.ServletRequest.getParameterMap()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.ServletRequest.getParameter(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "javax.servlet.ServletRequest.getInputStream()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));

        sign = "jakarta.servlet.ServletRequest.getReader()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "jakarta.servlet.ServletRequest.getParameterValues(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "jakarta.servlet.ServletRequest.getParameterNames()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "jakarta.servlet.ServletRequest.getParameter(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "jakarta.servlet.ServletRequest.getInputStream()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));

        sign = "org.springframework.web.util.pattern.PathPattern.getPatternString()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "org.springframework.web.method.support.HandlerMethodArgumentResolver.resolveArgument(org.springframework.core.MethodParameter,org.springframework.web.method.support.ModelAndViewContainer,org.springframework.web.context.request.NativeWebRequest,org.springframework.web.bind.support.WebDataBinderFactory)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));

        sign = "org.apache.commons.fileupload.FileUploadBase.parseRequest(org.apache.commons.fileupload.RequestContext)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));

        sign = "org.springframework.web.util.HtmlUtils.htmlEscape(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.springframework.web.util.HtmlUtils.htmlUnescape(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));
    }};
}
