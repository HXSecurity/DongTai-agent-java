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
    ;

    private final String key;

    private static final Map<String, TaintTag> LOOKUP = new HashMap<String, TaintTag>();

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
