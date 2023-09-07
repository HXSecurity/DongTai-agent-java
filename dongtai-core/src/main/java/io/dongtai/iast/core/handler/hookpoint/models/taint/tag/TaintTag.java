package io.dongtai.iast.core.handler.hookpoint.models.taint.tag;

import java.util.HashMap;
import java.util.Map;

public enum TaintTag {
    UNTRUSTED("untrusted"),
    CROSS_SITE("cross-site"),
    XSS_ENCODED("xss-encoded"),
    HTML_ENCODED("html-encoded"),
    HTML_DECODED("html-decoded"),
    URL_ENCODED("url-encoded"),
    URL_DECODED("url-decoded"),
    BASE64_ENCODED("base64-encoded"),
    BASE64_DECODED("base64-decoded"),
    JS_ENCODED("js-encoded"),
    JS_DECODED("js-decoded"),
    JSON_ENCODED("json-encoded"),
    JSON_DECODED("json-decoded"),
    XML_ENCODED("xml-encoded"),
    XML_DECODED("xml-decoded"),
    CSV_ENCODED("csv-encoded"),
    CSV_DECODED("csv-decoded"),
    SQL_ENCODED("sql-encoded"),
    SQL_DECODED("sql-decoded"),
    FTL_ENCODED("ftl-encoded"),
    FTL_DECODED("ftl-decoded"),
    CSS_ENCODED("css-encoded"),
    XPATH_ENCODED("xpath-encoded"),
    XPATH_DECODED("xpath-decoded"),
    LDAP_ENCODED("ldap-encoded"),
    LDAP_DECODED("ldap-decoded"),
    OS_ENCODED("os-encoded"),
    VBSCRIPT_ENCODED("vbscript-encoded"),
    HTTP_TOKEN_LIMITED_CHARS("http-token-limited-chars"),
    NUMERIC_LIMITED_CHARS("numeric-limited-chars"),
    CUSTOM_ENCODED_CMD_INJECTION("custom-encoded-cmd-injection"),
    CUSTOM_DECODED_CMD_INJECTION("custom-decoded-cmd-injection"),
    CUSTOM_ENCODED_JNDI_INJECTION("custom-encoded-jndi-injection"),
    CUSTOM_DECODED_JNDI_INJECTION("custom-decoded-jndi-injection"),
    CUSTOM_ENCODED_HQL_INJECTION("custom-encoded-hql-injection"),
    CUSTOM_DECODED_HQL_INJECTION("custom-decoded-hql-injection"),
    CUSTOM_ENCODED_NOSQL_INJECTION("custom-encoded-nosql-injection"),
    CUSTOM_DECODED_NOSQL_INJECTION("custom-decoded-nosql-injection"),
    CUSTOM_ENCODED_SMTP_INJECTION("custom-encoded-smtp-injection"),
    CUSTOM_DECODED_SMTP_INJECTION("custom-decoded-smtp-injection"),
    CUSTOM_ENCODED_XXE("custom-encoded-xxe"),
    CUSTOM_DECODED_XXE("custom-decoded-xxe"),
    CUSTOM_ENCODED_EL_INJECTION("custom-encoded-el-injection"),
    CUSTOM_DECODED_EL_INJECTION("custom-decoded-el-injection"),
    CUSTOM_ENCODED_REFLECTION_INJECTION("custom-encoded-reflection-injection"),
    CUSTOM_DECODED_("custom-decoded-reflection-injection"),
    CUSTOM_ENCODED_SSRF("custom-encoded-ssrf"),
    CUSTOM_DECODED_SSRF("custom-decoded-ssrf"),
    CUSTOM_ENCODED_PATH_TRAVERSAL("custom-encoded-path-traversal"),
    CUSTOM_DECODED_PATH_TRAVERSAL("custom-decoded-path-traversal"),
    CUSTOM_ENCODED_FILE_WRITE("custom-encoded-file-write"),
    CUSTOM_DECODED_FILE_WRITE("custom-encoded-file-write"),
    CUSTOM_ENCODED_REDOS("custom-encoded-redos"),
    CUSTOM_DECODED_REDOS("custom-decoded-redos"),
    VALIDATED("validated"),
    ;

    private final String key;

    private static final Map<String, TaintTag> LOOKUP = new HashMap<>();

    static {
        for (TaintTag t : TaintTag.values()) {
            LOOKUP.put(t.key, t);
        }
    }

    TaintTag(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public boolean equals(String key) {
        return this.key.equalsIgnoreCase(key);
    }

    public static TaintTag get(String name) {
        name = name.toLowerCase();
        return LOOKUP.get(name);
    }
}
