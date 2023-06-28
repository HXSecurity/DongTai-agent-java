package io.dongtai.iast.api.gather.dubbo.extractor;

import io.dongtai.iast.api.gather.dubbo.convertor.ServiceConvertor;
import io.dongtai.iast.api.openapi.convertor.OpenApiSchemaConvertorManager;
import io.dongtai.iast.api.openapi.domain.OpenApi;
import io.dongtai.iast.api.openapi.domain.Path;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 两个dubbo分支Service收集共同的逻辑抽象到这里
 * <p>
 * 需要兼容两个版本，如果2.6.x及以下版本，可以使用：com.alibaba.dubbo，2.7.0开始，直接使用org.apache.dubbo
 * <p>
 *
 * @author CC11001100
 * @since v1.12.0
 */
public abstract class AbstractDubboServiceExtractor {

    // 每个Gather共享同一个Manager
    private OpenApiSchemaConvertorManager manager;

    public AbstractDubboServiceExtractor() {
        this.manager = new OpenApiSchemaConvertorManager();
    }

    public OpenApi extract() {
        Object protocolObject = this.getProtocol("dubbo");
        if (protocolObject == null) {
            DongTaiLog.error("AbstractDubboServiceGather getProtocol null");
            return null;
        }
        Object exporterMap = this.getExporterMap(protocolObject);
        if (exporterMap == null) {
            DongTaiLog.error("AbstractDubboServiceGather getExporterMap null");
            return null;
        }
        List<Class> exportedServiceList = this.parseExportedServiceClassList(exporterMap);
        if (exportedServiceList == null || exportedServiceList.isEmpty()) {
            DongTaiLog.error("AbstractDubboServiceGather parseExportedServiceClassList empty");
            return null;
        }

        // 解析服务列表
        OpenApi openApi = new OpenApi();

        // 解析对外暴露的服务
        openApi.setPaths(this.parsePaths(exportedServiceList));

        // 涉及到的组件库
        openApi.setComponentsBySchemaMap(this.manager.getDatabase().toComponentSchemasMap());

        return openApi;
    }

    /**
     * 解析导出的类
     *
     * @param exportedServiceList
     * @return
     */
    private Map<String, Path> parsePaths(List<Class> exportedServiceList) {
        Map<String, Path> pathMap = new HashMap<>();
        exportedServiceList.forEach(new Consumer<Class>() {
            @Override
            public void accept(Class aClass) {
                try {
                    Map<String, Path> convert = new ServiceConvertor(manager, aClass).convert();
                    // 暂不考虑key覆盖的问题
                    pathMap.putAll(convert);
                } catch (Throwable e) {
                    DongTaiLog.error("AbstractDubboServiceGather parsePaths error", e);
                }
            }
        });
        return pathMap;
    }

    /**
     * 根据协议名获取协议对象
     *
     * @param protocolName example: dubbo
     * @return
     */
    private Object getProtocol(String protocolName) {
        Object protocolObj = this.getProtocolObject(protocolName);
        for (int i = 0; i < 10; i++) {
            try {
                Field protocolField = protocolObj.getClass().getDeclaredField("protocol");
                protocolField.setAccessible(true);
                protocolObj = protocolField.get(protocolObj);
                if (protocolObj.getClass() == this.exceptedProtocolClass()) {
                    break;
                }
            } catch (Throwable e) {
                DongTaiLog.error("AbstractDubboServiceGather getProtocol error", e);
            }
        }
        return protocolObj;
    }

    /**
     * 获取协议的对象
     *
     * @param protocolName
     * @return
     */
    protected abstract Object getProtocolObject(String protocolName);

    /**
     * 期望的协议类
     *
     * @return
     */
    protected abstract Class exceptedProtocolClass();

    /**
     * 从SPI的协议对象上获取导出的Map
     *
     * @param protocolObject
     * @return
     */
    private Object getExporterMap(Object protocolObject) {
        try {
            Field exporterMapField = protocolObject.getClass().getSuperclass().getDeclaredField("exporterMap");
            exporterMapField.setAccessible(true);
            return exporterMapField.get(protocolObject);
        } catch (Throwable e) {
            DongTaiLog.error("AbstractDubboServiceGather getExporterMap error", e);
        }
        return null;
    }

    /**
     * 从导出的服务表中解析出Service的class列表
     *
     * @param exporterMap Dubbo导出服务的map
     * @return
     */
    protected abstract List<Class> parseExportedServiceClassList(Object exporterMap);

}
