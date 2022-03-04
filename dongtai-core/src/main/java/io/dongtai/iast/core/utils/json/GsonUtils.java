package io.dongtai.iast.core.utils.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Gson工具类
 *
 * @author chenyi
 * @date 2022/3/3
 */
public class GsonUtils {

    private static final Gson GSON;

    static {
        GSON = new Gson();
    }

    private GsonUtils() {
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> Collection<T> toList(String json, Class<T> clazz) {
        Type collectionType = new TypeToken<Collection<T>>() {
        }.getType();
        return GSON.fromJson(json, collectionType);
    }

    public static <K, V> Map<K, V> toMap(String json, Class<Map<K, V>> clazz) {
        Type mapType = new TypeToken<Map<K, V>>() {
        }.getType();
        return GSON.fromJson(json, mapType);
    }

}