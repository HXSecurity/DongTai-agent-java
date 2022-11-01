package io.dongtai.iast.core.handler.hookpoint.vulscan;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;

/**
 * 定义漏洞扫描的接口
 *
 * @author dongzhiyong@huoxian.cn
 */
public interface IVulScan {
    /**
     * scan vul
     *
     * @param event    current method event
     * @param sinkNode current sink policy node
     */
    void scan(MethodEvent event, SinkNode sinkNode);
}
