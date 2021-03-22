package com.secnium.iast.core.handler.vulscan.overpower.JDBCImpl;

import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.overpower.IJdbc;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MySqlImpl implements IJdbc {
    private MethodEvent event;
    private String fullSql;

    @Override
    public boolean matchJdbc(String classname) {
        return classname.startsWith("com.mysql.");
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

    public String setAndReadSql() {
        // fixme: 暂时直接定义，如果后续在JDBC中出现不同的索引位置，进行更改
        int index = 4;
        if (null == event.argumentArray[1]) {
            Object sqlPackage = event.argumentArray[index - 1];
            try {
                Object data = sqlPackage.getClass().getMethod("getByteBuffer").invoke(sqlPackage);
                Object position = sqlPackage.getClass().getMethod("getPosition").invoke(sqlPackage);
                // todo: 将sql语句与用户输入数据进行关联
                fullSql = new String(Arrays.copyOfRange((byte[]) data, 1, (Integer) position));
                return fullSql;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Object[] readParams() {
        return new Object[]{fullSql};
    }
}
