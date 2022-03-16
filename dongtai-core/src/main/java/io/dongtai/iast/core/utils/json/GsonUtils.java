package io.dongtai.iast.core.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
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
        GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    private GsonUtils() {
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T toObject(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
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

    /**
     * 尝试将基本类型的字符串转换为对象
     */
    public static <T> T castBaseTypeString2Obj(String baseTypeValueStr, Class<T> clazz) throws IOException {
        TypeAdapter<T> typeAdapter = GSON.getAdapter(TypeToken.get(clazz));
        T object = typeAdapter.read(new JsonReader(new StringReader(baseTypeValueStr)));
        return Primitives.wrap(clazz).cast(object);
    }

}