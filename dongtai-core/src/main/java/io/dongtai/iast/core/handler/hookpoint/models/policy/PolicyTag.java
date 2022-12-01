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

        // source javax HttpServletRequest
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

        // source jakarta HttpServletRequest
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

        // source javax ServletRequest
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

        // source jakarta ServletRequest
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

        // source springframework
        sign = "org.springframework.web.util.pattern.PathPattern.getPatternString()";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));
        sign = "org.springframework.web.method.support.HandlerMethodArgumentResolver.resolveArgument(org.springframework.core.MethodParameter,org.springframework.web.method.support.ModelAndViewContainer,org.springframework.web.context.request.NativeWebRequest,org.springframework.web.bind.support.WebDataBinderFactory)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));

        // source fileupload
        sign = "org.apache.commons.fileupload.FileUploadBase.parseRequest(org.apache.commons.fileupload.RequestContext)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.CROSS_SITE}, new TaintTag[0]));

        // html
        sign = "sun.misc.CharacterEncoder.encode(java.io.InputStream,java.io.OutputStream)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "sun.misc.CharacterEncoder.encodeBuffer(java.io.InputStream,java.io.OutputStream)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "sun.misc.CharacterEncoder.decodeBuffer(java.io.InputStream,java.io.OutputStream)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));

        // html springframework
        sign = "org.springframework.web.util.HtmlUtils.htmlEscape(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.springframework.web.util.HtmlUtils.htmlEscape(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.springframework.web.util.HtmlUtils.htmlEscapeDecimal(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.springframework.web.util.HtmlUtils.htmlEscapeDecimal(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.springframework.web.util.HtmlUtils.htmlEscapeHex(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.springframework.web.util.HtmlUtils.htmlEscapeHex(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.springframework.web.util.HtmlUtils.htmlUnescape(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));

        // html apache
        sign = "org.apache.commons.lang.StringEscapeUtils.escapeHtml(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.apache.commons.lang.StringEscapeUtils.escapeHtml(java.io.Writer,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.apache.commons.lang3.StringEscapeUtils.escapeHtml3(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.apache.commons.lang.StringEscapeUtils.unescapeHtml(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));
        sign = "org.apache.commons.lang.StringEscapeUtils.unescapeHtml(java.io.Writer,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));
        sign = "org.apache.commons.lang3.StringEscapeUtils.unescapeHtml3(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));
        sign = "org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));

        // html unbescape
        sign = "org.unbescape.html.HtmlEscapeUtil.escape(java.lang.String,org.unbescape.html.HtmlEscapeType,org.unbescape.html.HtmlEscapeLevel)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.unbescape.html.HtmlEscapeUtil.escape(java.io.Reader,java.io.Writer,org.unbescape.html.HtmlEscapeType,org.unbescape.html.HtmlEscapeLevel)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.unbescape.html.HtmlEscapeUtil.escape(char[],int,int,java.io.Writer,org.unbescape.html.HtmlEscapeType,org.unbescape.html.HtmlEscapeLevel)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_ENCODED}, new TaintTag[]{TaintTag.HTML_DECODED}));
        sign = "org.unbescape.html.HtmlEscapeUtil.unescape(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));
        sign = "org.unbescape.html.HtmlEscapeUtil.unescape(java.io.Reader,java.io.Writer)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));
        sign = "org.unbescape.html.HtmlEscapeUtil.unescape(char[],int,int,java.io.Writer)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.HTML_DECODED}, new TaintTag[]{TaintTag.HTML_ENCODED}));

        // url
        sign = "java.net.URLEncoder.encode(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "java.net.URLEncoder.encode(java.lang.String,java.nio.charset.Charset)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "java.net.URLDecoder.decode(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_DECODED}, new TaintTag[]{TaintTag.URL_ENCODED, TaintTag.XSS_ENCODED}));
        sign = "java.net.URLDecoder.decode(java.lang.String,java.nio.charset.Charset)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_DECODED}, new TaintTag[]{TaintTag.URL_ENCODED, TaintTag.XSS_ENCODED}));

        // url javax HttpServletResponse
        sign = "javax.servlet.http.HttpServletResponse.encodeUrl(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "javax.servlet.http.HttpServletResponse.encodeURL(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "javax.servlet.http.HttpServletResponse.encodeRedirectUrl(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "javax.servlet.http.HttpServletResponse.encodeRedirectURL(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));

        // url springframework
        sign = "org.springframework.web.util.UriUtils.encode(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeUri(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeHttpUrl(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeScheme(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeAuthority(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeUserInfo(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeHost(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodePort(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodePath(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodePathSegment(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeQuery(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeQueryParam(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encodeFragment(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.encode(java.lang.String,java.nio.charset.Charset)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));
        sign = "org.springframework.web.util.UriUtils.decode(java.lang.String,java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_DECODED}, new TaintTag[]{TaintTag.URL_ENCODED}));
        sign = "org.springframework.util.StringUtils.uriDecode(java.lang.String,java.nio.charset.Charset)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_DECODED}, new TaintTag[]{TaintTag.URL_ENCODED}));

        // url apache
        sign = "org.apache.catalina.util.URLEncoder.encode(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.URL_ENCODED}, new TaintTag[]{TaintTag.URL_DECODED}));

        // base64 springframework
        sign = "org.springframework.webflow.util.Base64.encode(byte[])";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.BASE64_ENCODED}, new TaintTag[]{TaintTag.BASE64_DECODED}));
        sign = "org.springframework.webflow.util.Base64.encode(byte[],int,int)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.BASE64_ENCODED}, new TaintTag[]{TaintTag.BASE64_DECODED}));
        sign = "org.springframework.webflow.util.Base64.encodeToString(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.BASE64_ENCODED}, new TaintTag[]{TaintTag.BASE64_DECODED}));
        sign = "org.springframework.webflow.util.Base64.decode(byte[])";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.BASE64_DECODED}, new TaintTag[]{TaintTag.BASE64_ENCODED}));
        sign = "org.springframework.webflow.util.Base64.decode(byte[],int,int)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.BASE64_DECODED}, new TaintTag[]{TaintTag.BASE64_ENCODED}));
        sign = "org.springframework.webflow.util.Base64.decodeFromString(java.lang.String)";
        put(sign, Arrays.asList(new TaintTag[]{TaintTag.BASE64_DECODED}, new TaintTag[]{TaintTag.BASE64_ENCODED}));
    }};
}
