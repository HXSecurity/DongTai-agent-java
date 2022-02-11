package io.dongtai.iast.core.bytecode.enhance.plugin.autobinding;

import io.dongtai.iast.core.utils.ObjectShare;
import io.dongtai.iast.core.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SpringAutoBindingDispatchImpl {
    public void onDoBind(Object paramObject1, Object paramObject2) throws Throwable {
        if (paramObject1 != null && paramObject2 != null) {
            try {

                Method method1 = ReflectUtils.getPublicMethodFromClass(paramObject2.getClass(), "getAllowedFields");
                String[] arrayOfString1 = (String[]) method1.invoke(paramObject2, ObjectShare.EMPTY_OBJ_ARRAY);

                Method method2 = ReflectUtils.getPublicMethodFromClass(paramObject2.getClass(), "getDisallowedFields");
                String[] arrayOfString2 =
                        (String[]) method2.invoke(paramObject2, ObjectShare.EMPTY_OBJ_ARRAY);

                if (arrayOfString1 == null && (arrayOfString2 == null || arrayOfString2.length == 0)) {
                    Class clazz = paramObject1.getClass().getSuperclass();
                    Field field = ReflectUtils.getFieldFromClass(clazz, "methodResolver");
                    Object object = field.get(paramObject1);
                    if (object != null) {
                        Class clazz1 = object.getClass().getSuperclass();
                        Field field1 = ReflectUtils.getFieldFromClass(clazz1, "handlerMethods");
                        Set set = (Set) field1.get(object);
                        if (set.size() > 0) {
                            a((Method) set.toArray()[0]);
                        }
                    }

                }
            } catch (Exception exception) {

            }
        }
    }

    private void a(Method paramMethod) {
//        System.out.println("Spring Auto Binding Vuln for Method: " + paramMethod);
    }
}
