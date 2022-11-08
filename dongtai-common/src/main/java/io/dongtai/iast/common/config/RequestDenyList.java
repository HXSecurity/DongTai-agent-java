package io.dongtai.iast.common.config;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class RequestDenyList {
    private List<List<RequestDeny>> denies = new ArrayList<List<RequestDeny>>();

    public static RequestDenyList parse(JSONArray config) {
        if (config == null || config.length() == 0) {
            return null;
        }

        RequestDenyList denyList = new RequestDenyList();
        int orLen = config.length();
        for (int i = 0; i < orLen; i++) {
            JSONArray andConfig = config.getJSONArray(i);
            if (andConfig == null || andConfig.length() == 0) {
                continue;
            }
            int andLen = andConfig.length();
            List<RequestDeny> andList = new ArrayList<RequestDeny>();
            for (int j = 0; j < andLen; j++) {
                RequestDeny requestDeny = RequestDeny.parse(andConfig.getJSONObject(j));
                if (requestDeny == null) {
                    continue;
                }
                andList.add(requestDeny);
            }
            if (!andList.isEmpty()) {
                denyList.denies.add(andList);
            }
        }

        if (denyList.denies.isEmpty()) {
            return null;
        }
        return denyList;
    }
}
