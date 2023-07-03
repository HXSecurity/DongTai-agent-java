package io.dongtai.iast.api.gather.dubbo.extractor;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import io.dongtai.iast.api.openapi.domain.OpenApi;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 用于收集alibaba包的dubbo的接口
 *
 * <a href="https://repo1.maven.org/maven2/com/alibaba/dubbo/">Alibaba Dubbo Version List</a>
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class AlibabaDubboServiceExtractor extends AbstractDubboServiceExtractor {

    /**
     * 静态方法供反射调用
     *
     * @return
     */
    public static OpenApi run() {
        return new AlibabaDubboServiceExtractor().extract();
    }

    @Override
    protected Object getProtocolObject(String protocolName) {
        return ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(protocolName);
    }

    @Override
    protected Class exceptedProtocolClass() {
        return DubboProtocol.class;
    }

    @Override
    protected List<Class> parseExportedServiceClassList(Object exporterMap) {
        List<Class> serviceClassList = new ArrayList<>();
        try {
            // 除 2.4.11 之外的版本，即 [2.0.10, 2.4.11) 和 (2.4.11, 2.6.12] 两个版本范围
            // 2.4.11 版本无法解析
            // 2023-6-26 18:49:15 发现2.4.11是一个无效的发布包，已经跟阿里开源反馈，希望他们能处理一下....
            // 2023-6-28 16:35:45 收到了回复，他们放弃维护，不删除...那到时候有人问再解释吧...
            ((Map<String, Exporter>) exporterMap).forEach(new BiConsumer<String, Exporter>() {
                @Override
                public void accept(String s, Exporter exporter) {
                    Class serviceClass = exporter.getInvoker().getInterface();
                    serviceClassList.add(serviceClass);
                }
            });
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.API_GATHER_DUBBO_ALIBABA_PARSE_SERVICE_LIST_ERROR, e);
        }
        return serviceClassList;
    }

}
