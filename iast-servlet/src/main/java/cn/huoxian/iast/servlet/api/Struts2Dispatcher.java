package cn.huoxian.iast.servlet.api;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.RuntimeConfiguration;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import org.apache.struts2.dispatcher.Dispatcher;

import java.util.*;

public class Struts2Dispatcher {
    public static Map<String, Object> getAPI(Object dispatcher) {
        return createReport(getAPIList((Dispatcher) dispatcher));
    }

    public static List<ApiDataModel> getAPIList(Dispatcher dispatcher) {

        Set<String> apiSet = new HashSet<>();
        ConfigurationManager cm = dispatcher.getConfigurationManager();
        Configuration cf = cm.getConfiguration();
        RuntimeConfiguration rc = cf.getRuntimeConfiguration();
        Map<String, Map<String, ActionConfig>> d = rc.getActionConfigs();
        for (String key1 : d.keySet())
        {
            apiSet.add(key1);
            Map<String, ActionConfig> a = d.get(key1);
            apiSet.addAll(a.keySet());
        }

        List<ApiDataModel>apiList = new ArrayList<>();
        for (String s : apiSet) {
            ApiDataModel apiDataModel = new ApiDataModel();
            if (!s.contains("/")){
                s = "/"+s;
            }
            apiDataModel.setUrl(s);
            apiDataModel.setMethod(new String[]{"GET", "POST"});
            apiList.add(apiDataModel);
        }

        return apiList;
    }

    private static Map<String, Object> createReport(List<ApiDataModel> apiList) {
        Map<String, Object> apiDataReport = new HashMap<>();
        List<Object> apiData = new ArrayList<>();
        for (ApiDataModel apiDataModel:apiList
        ) {
            Map<String, Object> api = new HashMap<>();
            apiData.add(api);
            api.put("uri",apiDataModel.getUrl());
            String[] methods = apiDataModel.getMethod();
            List<Object> methodsjson = new ArrayList<>(Arrays.asList(methods));
            api.put("method",methodsjson);
            api.put("class",apiDataModel.getClazz());
            List<Map<String, String>> parameters = apiDataModel.getParameters();
            List<Object> parametersJson = new ArrayList<>();
            api.put("parameters",parametersJson);
            if (parameters != null){
                for (Map<String,String> parameter:parameters
                ) {
                    Map<String, Object> parameterjson = new HashMap<>();
                    parametersJson.add(parameterjson);
                    parameterjson.put("name",parameter.get("name"));
                    parameterjson.put("type",parameter.get("type"));
                    parameterjson.put("annotation",parameter.get("annotation"));
                }
            }
            api.put("return_type",apiDataModel.getReturnType());
            api.put("file",apiDataModel.getFile());
            api.put("controller",apiDataModel.getController());
            api.put("description",apiDataModel.getDescription());
        }
        apiDataReport.put("api_data",apiData);
        return apiDataReport;
    }
}
