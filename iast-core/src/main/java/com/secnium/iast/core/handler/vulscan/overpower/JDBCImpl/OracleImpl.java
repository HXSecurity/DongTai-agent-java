package com.secnium.iast.core.handler.vulscan.overpower.JdbcImpl;

import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.overpower.IJdbc;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class OracleImpl implements IJdbc {
    private MethodEvent event;
    private String fullSql = null;

    @Override
    public boolean matchJdbc(String classname) {
        return false;
    }

    @Override
    public void setEvent(MethodEvent event) {

    }

    @Override
    public void removeEvent() {
        this.fullSql = null;
        this.event = null;
    }

    @Override
    public String readSql() {
        return null;
    }

    @Override
    public Object[] readParams() {
        return new Object[0];
    }
}
