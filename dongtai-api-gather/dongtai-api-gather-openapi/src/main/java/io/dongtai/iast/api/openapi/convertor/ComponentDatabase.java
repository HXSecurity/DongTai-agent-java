package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;

import java.util.*;
import java.util.function.Consumer;

/**
 * 用于集中存储管理Schema的数据库
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class ComponentDatabase {

    // 类到Schema的映射
    private final Map<Class, Schema> classToSchemaMap;

    // 已经发现了的类，用于避免重复处理，也避免碰到循环引用时递归爆栈
    private Set<Class> existsClassSet = new HashSet<>();

    // 符合类型的schema生成完毕的时候的回调方法，用于处理环形依赖
    private final Map<Class, List<Consumer<Schema>>> classSchemaDoneCallbackMap;

    private final OpenApiSchemaConvertorManager manager;

    public ComponentDatabase(OpenApiSchemaConvertorManager manager) {
        this.manager = manager;
        this.classToSchemaMap = new HashMap<>();
        this.classSchemaDoneCallbackMap = new HashMap<>();
        this.existsClassSet = new HashSet<>();
    }

    /**
     * 是否已经发现过此类型
     *
     * @param clazz
     * @return
     */
    public boolean exists(Class clazz) {
        return existsClassSet.contains(clazz);
    }

    /**
     * 把类标记为已发现
     *
     * @param clazz
     */
    public void addExists(Class clazz) {
        existsClassSet.add(clazz);
    }

    /**
     * 为类型注册一个转换完成的回调方法
     *
     * @param clazz
     * @param consumer
     */
    public void addSchemaConvertDoneCallback(Class clazz, Consumer<Schema> consumer) {
        List<Consumer<Schema>> consumers = classSchemaDoneCallbackMap.computeIfAbsent(clazz, k -> new ArrayList<>());
        consumers.add(consumer);
    }

    /**
     * 触发类的回调方法
     *
     * @param clazz 被触发的类
     * @param c     类处理后的组件
     */
    public void triggerSchemaCallback(Class clazz, Schema c) {
        List<Consumer<Schema>> consumers = classSchemaDoneCallbackMap.get(clazz);
        if (consumers == null) {
            return;
        }
        consumers.forEach(x -> x.accept(c));
    }

    /**
     * 往数据库中存储组件，不是调用了就一定会存储的，只会存储非数组、非原生、非集合类型
     *
     * @param clazz
     * @param c
     * @return
     */
    public boolean store(Class clazz, Schema c) {
        if (canStore(clazz)) {
            classToSchemaMap.put(clazz, c);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断此类型是否能够存储
     *
     * @param clazz
     * @return
     */
    public boolean canStore(Class clazz) {
        if (clazz == null) {
            return false;
        }
        return this.manager.javaBeanOpenApiSchemaConvertor.canConvert(clazz);
    }

    /**
     * 根据类型查询组件，不存在的话返回null
     *
     * @param clazz
     * @return
     */
    public Schema find(Class clazz) {
        return this.classToSchemaMap.get(clazz);
    }

    /**
     * 把当前存储的所有的schema转换为map形式，为了后续拼接完整的open api格式方便
     *
     * @return
     */
    public Map<String, Schema> toComponentSchemasMap() {
        Map<String, Schema> m = new HashMap<>();
        classToSchemaMap.forEach((aClass, component) -> m.put(component.getName(), component));
        return m;
    }

//    /**
//     * 把给定的类注册到组件库中，但是并不解析名称，暂时未用到先注释掉
//     *
//     * @param clazz
//     * @return
//     */
//    public Schema register(Class clazz) {
//        Schema c = new Schema();
//        c.setType("object");
//        c.setName(clazz.getName());
//        classToSchemaMap.put(clazz, c);
//        return c.direct();
//    }

}
