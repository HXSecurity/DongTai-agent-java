package com.secnium.iast.core.handler.vulscan.overpower.JDBCImpl;

import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.overpower.IJdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SqlServerImpl implements IJdbc {

    private MethodEvent event;
    private String fullSql = null;

    @Override
    public boolean matchJdbc(String classname) {
        return classname.startsWith("com.microsoft.sqlserver.");
    }

    @Override
    public void setEvent(MethodEvent event) {
        this.event = event;
    }

    @Override
    public void removeEvent() {
        this.fullSql = null;
        this.event = null;
    }

    @Override
    public String readSql() {
        return fullSql == null ? setAndReadSql() : fullSql;
    }

    private String setAndReadSql() {
        try {
            Class<?> stClass = event.object.getClass();
            Field stField = stClass.getDeclaredField("userSQL");
            stField.setAccessible(true);
            fullSql = (String) stField.get(event.object);
            return fullSql;
        } catch (Exception e) {
            return (fullSql = "");
        }
    }

    @Override
    public Object[] readParams() {
        try {
            Class<?> stClass = event.object.getClass();
            Field paramField = stClass.getSuperclass().getDeclaredField("inOutParam");
            paramField.setAccessible(true);
            Object params = paramField.get(event.object);
            if (params != null) {
                Object[] paramFields = (Object[]) params;
                Object[] paramValues = new Object[paramFields.length];
                for (int i = 0; i < paramFields.length; i++) {
                    Field setterDTVField = paramFields[i].getClass().getDeclaredField("setterDTV");
                    setterDTVField.setAccessible(true);
                    Object setterDTVValue = setterDTVField.get(paramFields[i]);

                    Method method = setterDTVValue.getClass().getDeclaredMethod("getSetterValue");
                    method.setAccessible(true);
                    paramValues[i] = method.invoke(setterDTVValue);
                }
                return paramValues;
            }
        } catch (NoSuchFieldException e) {
            return null;
        } catch (Exception e) {
            // fixme 处理足够异常，适配各种版本的框架和中间件
            return null;
        }
        return null;
    }
}
