package com.secnium.iast.core.handler.vulscan;

/**
 * @author dongzhiyong@huoxian.cn
 */

public enum VulnType {

    /**
     * 漏洞
     */
    SQL_OVER_POWER("sql-over-power", "info", false),
    SMTP_INJECTION("smtp-injection", "high", true),
    SSRF("ssrf", "high", true),
    UNSAFE_XML_DECODE("unsafe-xml-decode", "high", true),
    CMD_INJECTION("cmd-injection", "high", true),
    SQL_INJECTION("sql-injection", "high", true),
    NOSQL_INJECTION("nosql-injection", "high", true),
    HQL_INJECTION("hql-injection", "high", true),
    LDAP_INJECTION("ldap-injection", "high", true),
    XPATH_INJECTION("xpath-injection", "high", true),
    PATH_TRAVERSAL("path-traversal", "high", true),
    REFLECTED_XSS("reflected-xss", "high", true),
    XXE("xxe", "high", true),
    EXPRESSION_LANGUAGE_INJECTION("expression-language-injection", "high", true),
    REFLECTION_INJECTION("reflection-injectionl", "high", true),
    UNSAFE_JSON_DESERIALIZE("unsafe-json-deserialize", "high", true),
    UNSAFE_READLINE("unsafe-readline", "high", true),
    UNVALIDATED_FORWARD("unvalidated-forward", "high", true),
    UNVALIDATED_REDIRECT("unvalidated-redirect", "high", true),
    HEADER_INJECTION("header-injection", "high", true),
    DYNAMIC_LIBRARY_LOAD("dynamic-library-load", "high", true),
    SPRING_CLOUD_CONFIG_SERVER("spring-cloud-config-server", "high", true),

    REDOS("redos", "high", true),

    CRYPTO_WEEK_RANDOMNESS("crypto-weak-randomness", "low", false),
    CRYPTO_BAC_CIPHERS("crypto-bad-ciphers", "high", false),
    CRYPTO_BAD_MAC("crypto-bad-mac", "high", false),
    COOKIE_FLAGS_MISSING("cookie-flags-missing", "high", true),
    TRUST_BOUNDARY_VIOLATION("trust-boundary-violation", "high", true),

    UNKNOWN("unknown", "info", false);


    public String getName() {
        return name;
    }

    /**
     * 漏洞类型 值
     */
    String name;
    String weight;
    boolean tracked;

    VulnType(String name, String weight, boolean tracked) {
        this.name = name;
        this.weight = weight;
        this.tracked = tracked;
    }


    public boolean equals(String name) {
        return this.name.equals(name);
    }

    public static VulnType getTypeByName(String name) {
        for (VulnType vType : VulnType.values()) {
            if (vType.equals(name)) {
                return vType;
            }
        }
        return null;
    }
}
