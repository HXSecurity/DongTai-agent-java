package com.secnium.iast.core.handler.vulscan.overpower.JDBCImpl;

import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.overpower.IJdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PostgresImpl implements IJdbc {
    private MethodEvent event;
    private String fullSql = null;

    @Override
    public boolean matchJdbc(String classname) {
        return classname.startsWith("org.postgresql.");
    }

    @Override
    public void setEvent(MethodEvent event) {
        this.event = event;
    }

    @Override
    public void removeEvent() {
        this.event = null;
        this.fullSql = null;
    }

    @Override
    public String readSql() {
        return fullSql == null ? setAndReadSql() : fullSql;
    }

    private String setAndReadSql() {
        try {
            Object cachedQuery = event.argumentArray[0];
            // 反射获取query对象
            Field queryField = cachedQuery.getClass().getDeclaredField("query");
            queryField.setAccessible(true);
            Object query = queryField.get(cachedQuery);

            Method methodOfGetNativeSql = query.getClass().getDeclaredMethod("getNativeSql");
            methodOfGetNativeSql.setAccessible(true);
            fullSql = (String) methodOfGetNativeSql.invoke(query);

            return fullSql;
        } catch (Exception e) {
            return (fullSql = "");
        }
    }

    @Override
    public Object[] readParams() {
        try {
            Object paramList = event.argumentArray[1];
            Method getValues = paramList.getClass().getDeclaredMethod("getValues");
            getValues.setAccessible(true);
            Object paramValue = getValues.invoke(paramList);
            if (paramValue != null && paramValue.getClass().isArray()) {
                return (Object[]) paramValue;
            }
        } catch (Exception e) {
            // fixme 处理足够异常，适配各种版本的框架和中间件
            return null;
        }
        return null;
    }

}
