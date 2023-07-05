package io.dongtai.iast.api.openapi.convertor;

import io.dongtai.iast.api.openapi.domain.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用于转换JavaBean到OpenApi的组件
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class JavaBeanOpenApiSchemaConvertor extends BaseOpenApiSchemaConvertor {

    public JavaBeanOpenApiSchemaConvertor(OpenApiSchemaConvertorManager manager) {
        super(manager);
    }

    @Override
    public String getConvertorName() {
        return "bean-convertor";
    }

    @Override
    public boolean canConvert(Class clazz) {
        // Bean转换器作为兜底的存在
        for (ClassOpenApiSchemaConvertor convertor : this.manager.convertors) {
            if (convertor.getClass() == this.getClass()) {
                continue;
            } else if (convertor.canConvert(clazz)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 把一个符合JavaBean规范的类转换为Open API的schema格式
     *
     * @param clazz
     * @return
     */
    @Override
    public Schema convert(Class clazz) {

        // 先查下当前转换任务的Open API组件库，如果有的话就直接返回即可
        Schema schema = manager.database.find(clazz);
        if (schema != null) {
            return schema.direct();
        }

        // 如果在已经发现的类型列表中但是数据库中又没有，则说明已经发现了正在处理中还没有处理完成，则注册一个回调方法，在处理完成的时候拿一下结果
        if (manager.database.exists(clazz)) {
            Schema c = new Schema();
            manager.database.addSchemaConvertDoneCallback(clazz, x -> c.set$ref(x.generateRef()));
            return c;
        }

        // 这个类没有被发现过，这是第一次处理它，将类标记为已发现，防止DFS的时候陷入环形依赖
        manager.database.addExists(clazz);

        // 然后开始处理它
        Schema c = new Schema();
        // 与类的短名字保持一致，这里会发生名称冲突吗？
        c.setName(clazz.getSimpleName());
        c.setType("object");

        // 处理类上的字段，向上递归处理所有字段，并检查是否符合Bean规范
        parseFieldList(clazz).forEach(field -> c.addProperty(field.getName(), convert(clazz, field)));

        // 把转换完的组件存储一下
        manager.database.store(clazz, c);

        // 在类处理完的时候触发一下回调
        manager.database.triggerSchemaCallback(clazz, c);

        return c;
    }

    // 此处暂不考虑继承泛型的问题，下个版本再处理它
    private List<Field> parseFieldList(Class clazz) {

        List<Field> allFieldList = new ArrayList<>();
        Set<String> fieldNameSet = new HashSet<>();
        Set<String> setterNameLowercaseSet = new HashSet<>();
        Class currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {

            // 收集类上的字段，可能会发生字段覆盖的情况，所以先收集再处理，而不是边处理边收集
            Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field f : declaredFields) {
                if (fieldNameSet.contains(f.getName())) {
                    continue;
                }
                allFieldList.add(f);
                fieldNameSet.add(f.getName());
            }

            // 收集类上的方法名字
            setterNameLowercaseSet.addAll(parseSetterNameLowercaseSet(currentClass));

            // 再处理父类，一路向上知道找到根
            currentClass = currentClass.getSuperclass();
        }

        // 然后筛选出来符合条件的字段，作为bean的属性
        List<Field> beanFieldList = new ArrayList<>();
        allFieldList.forEach(field -> {
            if (isBeanField(field, setterNameLowercaseSet)) {
                beanFieldList.add(field);
            }
        });

        return beanFieldList;
    }

    /**
     * 判断Field是否是bean的field
     *
     * @param field
     * @param setterNameLowercaseSet
     * @return
     */
    private boolean isBeanField(Field field, Set<String> setterNameLowercaseSet) {

        // 采用白名单的方式，public并且是实例方法则认为是可以的
        if (Modifier.isPublic(field.getModifiers())) {
            return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
        }

        // 私有方法并且有对应的getter
        String setterMethodName = "set" + field.getName().toLowerCase();
        return setterNameLowercaseSet.contains(setterMethodName);
    }

    /**
     * 解析类上的setter方法，并将其方法名都转为小写返回
     *
     * @param clazz
     * @return
     */
    private Set<String> parseSetterNameLowercaseSet(Class clazz) {
        Set<String> setterNameLowercaseSet = new HashSet<>();
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            // 这里采用比较简单的策略，只要是关键字开头的就认为是ok的
            if (declaredMethod.getName().startsWith("set")) {
                setterNameLowercaseSet.add(declaredMethod.getName().toLowerCase());
            }
        }
        return setterNameLowercaseSet;
    }

    @Override
    public Schema convert(Class clazz, Field field) {
        // 因为类型可能是各种类型，所以这里要调用manager上的来路由
        return manager.convertClass(field.getType());
    }

}
