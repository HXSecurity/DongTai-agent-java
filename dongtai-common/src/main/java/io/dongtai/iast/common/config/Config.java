package io.dongtai.iast.common.config;

public class Config<T> {
    private final ConfigKey key;
    private T defaultValue;
    private T value;

    public Config(ConfigKey key) {
        this.key = key;
    }

    public static <T> Config<T> create(ConfigKey key) {
        return new Config<T>(key);
    }

    public ConfigKey getKey() {
        return key;
    }

    public Config<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T get() {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
