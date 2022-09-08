package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

enum Feature {
    ACCESS_EXTERNAL_DTD("http://javax.xml.XMLConstants/property/accessExternalDTD"),
    ACCESS_EXTERNAL_STYLESHEET("http://javax.xml.XMLConstants/property/accessExternalStylesheet"),
    ACCESS_EXTERNAL_SCHEMA("http://javax.xml.XMLConstants/property/accessExternalSchema"),
    DISALLOW_DOCTYPE("http://apache.org/xml/features/disallow-doctype-decl") {
        @Override
        public boolean isSupport(Support support) {
            return support == Support.ALLOWED;
        }
    },
    EXTERNAL_GENERAL("http://xml.org/sax/features/external-general-entities"),
    EXTERNAL_PARAMETER("http://xml.org/sax/features/external-parameter-entities"),
    LOAD_EXTERNAL_DTD("http://apache.org/xml/features/nonvalidating/load-external-dtd"),
    XINCLUDE_AWARE("http://apache.org/xml/features/xinclude");

    private final Object[] dtd;

    Feature(String dtd) {
        this.dtd = new Object[]{dtd};
    }

    public Object[] getDtd() {
        return this.dtd;
    }

    public boolean isSupport(Support support) {
        return support.isSupport();
    }
}
