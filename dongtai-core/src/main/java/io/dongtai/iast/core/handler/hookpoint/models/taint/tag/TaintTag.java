package io.dongtai.iast.core.handler.hookpoint.models.taint.tag;

public enum TaintTag {
    UNTRUSTED("untrusted"),
    CROSS_SITE("cross-site"),
    HTML_ENCODED("html-encoded"),
    HTML_DECODED("html-decoded"),
    XSS_ENCODED("xss-encoded"),
    ;

    private final String key;

    TaintTag(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
