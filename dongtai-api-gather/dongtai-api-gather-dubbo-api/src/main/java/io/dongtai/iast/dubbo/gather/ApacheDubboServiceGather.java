package io.dongtai.iast.dubbo.gather;

import io.dongtai.iast.openapi.domain.OpenApi;
import io.dongtai.log.DongTaiLog;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Protocol;
import org.apache.dubbo.rpc.protocol.DelegateExporterMap;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 用于解析apache包的dubbo
 * <p>
 * <a href="https://repo1.maven.org/maven2/org/apache/dubbo/dubbo/">Apache Dubbo Version List</a>
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class ApacheDubboServiceGather extends AbstractDubboServiceGather {

    /**
     * 静态方法供反射调用
     *
     * @return
     */
    public static OpenApi run() {
        return new ApacheDubboServiceGather().gather();
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

        // [2.7.13, 3.0.0) 之间的11个版本有个狗比把这个字段的类型修改为DelegateExporterMap了，后来又改回来了
        try {
            if (exporterMap instanceof DelegateExporterMap) {
                ((DelegateExporterMap) exporterMap).getExporterMap().forEach(new BiConsumer<String, Exporter<?>>() {
                    @Override
                    public void accept(String s, Exporter<?> exporter) {
                        serviceClassList.add(exporter.getInvoker().getInterface());
                    }
                });
                return serviceClassList;
            }
        } catch (Throwable e) {
            // TODO 2023-6-26 15:24:50 log
        }

        //  [2.7.9, 2.7.13) 和 (3.0.0, 3.2.0-beta.6] 之间都是 Map<String, Exporter<?>> 类型
        try {
            if (exporterMap instanceof Map) {
                ((Map<String, Exporter>) exporterMap).forEach(new BiConsumer<String, Exporter>() {
                    @Override
                    public void accept(String s, Exporter exporter) {
                        serviceClassList.add(exporter.getInvoker().getInterface());
                    }
                });
                return serviceClassList;
            }
        } catch (Throwable e) {
            // TODO 2023-6-26 15:25:47 log
            DongTaiLog.error("");
        }

        return serviceClassList;
    }

}
