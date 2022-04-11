package io.dongtai.iast.core.handler.hookpoint.service;

/**
 * service type
 */
public enum ServiceType {
    // service MySQL
    MYSQL("mysql", "db"),
    // service PostgreSQL
    POSTGRESQL("postgresql", "db"),
    // service Kafka
    KAFKA("kafka", "mq"),
    ;

    /**
     * service type
     */
    private final String type;
    /**
     * service category
     */
    private final String category;

    ServiceType(String type, String category) {
        this.type = type;
        this.category = category;
    }

    public String getType() {
        return this.type;
    }

    public String getCategory() {
        return this.category;
    }

    public boolean equals(String type) {
        return this.type.equals(type.toUpperCase());
    }
}
