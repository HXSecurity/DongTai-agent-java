package io.dongtai.iast.core.handler.hookpoint.api;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.SpringApplicationImpl;
import io.dongtai.iast.core.handler.hookpoint.models.KrpcApiModel;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class KrpcApiThread extends Thread {

    private final List<Object> listOfWebUrl;

    public KrpcApiThread(List<Object> listOfWebUrl) {
        this.listOfWebUrl = listOfWebUrl;
    }

    @Override
    public void run() {
        List<KrpcApiModel> apiSiteMap = getApiSiteMap(listOfWebUrl);
        for (KrpcApiModel krpcApiModel:apiSiteMap){
            EngineManager.KRPC_API_SITEMAP.put(krpcApiModel.getServiceId()+krpcApiModel.getMsgId(),krpcApiModel);
        }
        Map<String, Object> report = createReport(apiSiteMap);
        ApiReport.sendReport(report);
        EngineManager.KRPC_API_SITEMAP_IS_SEND = true;
    }

    private static List<KrpcApiModel> getApiSiteMap(List<Object> listOfWebUrl) {
        Object classOfWebUrl = listOfWebUrl.get(0);
        Class<?> WebUrl = classOfWebUrl.getClass();
        List<KrpcApiModel> krpcApiModelList = new ArrayList<>();
        try {
            Method methodOfGetPath = WebUrl.getMethod("getPath");
            Method methodOfGetServiceId = WebUrl.getMethod("getServiceId");
            Method methodOfGetMsgId = WebUrl.getMethod("getMsgId");
            Method methodOfGetHosts = WebUrl.getMethod("getHosts");
            Method methodOfGetMethods = WebUrl.getMethod("getMethods");
            for (Object url : listOfWebUrl) {
                String getPath = (String) methodOfGetPath.invoke(url);
                String getServiceId = String.valueOf(methodOfGetServiceId.invoke(url));
                String getMsgId = String.valueOf(methodOfGetMsgId.invoke(url));
                String getHosts = (String) methodOfGetHosts.invoke(url);
                String getMethods = (String) methodOfGetMethods.invoke(url);
                KrpcApiModel krpcApiModel = new KrpcApiModel(getHosts, getPath, getMethods, getServiceId, getMsgId);
                krpcApiModelList.add(krpcApiModel);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            DongTaiLog.error(e);
        }
        return krpcApiModelList;
    }

    private static Map<String, Object> createReport(List<KrpcApiModel> apiSiteMap) {
        Map<String, Object> apiDataReport = new HashMap<>();
        List<Object> apiData = new ArrayList<>();
        for (KrpcApiModel apiDataModel : apiSiteMap
        ) {
            Map<String, Object> api = new HashMap<>();
            apiData.add(api);
            api.put("uri", apiDataModel.getPath());
            String method = apiDataModel.getMethods().toUpperCase(Locale.ROOT);
            String[] methods = null;
            if (method.contains(",")) {
                methods = method.split(",");
            } else {
                methods = new String[]{method};
            }
            List<Object> methodsjson = new ArrayList<>(Arrays.asList(methods));
            api.put("method", methodsjson);
            api.put("class", "");
            api.put("parameters", "");
            api.put("returnType", "");
            api.put("file", "");
            api.put("controller", "");
            api.put("description", "");
        }
        apiDataReport.put("apiData", apiData);
        return apiDataReport;
    }

}