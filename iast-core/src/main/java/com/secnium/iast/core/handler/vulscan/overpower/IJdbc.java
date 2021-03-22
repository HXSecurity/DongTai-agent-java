package com.secnium.iast.core.handler.vulscan.overpower;

import com.secnium.iast.core.handler.models.MethodEvent;

/**
 * @author dongzhiyong@huoxian.cn
 */
public interface IJdbc {
    boolean matchJdbc(String classname);

    void setEvent(MethodEvent event);

    void removeEvent();

    String readSql();

    Object[] readParams();
}
