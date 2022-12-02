package io.dongtai.iast.core.handler.hookpoint.models.taint.tag;

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
    ;

    private final String key;

    TaintTag(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public boolean equals(String key) {
        return this.key.equals(key);
    }
}
