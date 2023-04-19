package io.dongtai.iast.common.config;

import org.json.JSONArray;

import java.util.*;

public class RequestDenyList {
    private final List<List<RequestDeny>> denies = new ArrayList<List<RequestDeny>>();

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

    public void addRule(List<RequestDeny> requestDenies) {
        this.denies.add(requestDenies);
    }

    public boolean match(String url, Map<String, String> headers) {
        boolean matched = false;
        for (List<RequestDeny> denyList : this.denies) {
            boolean subHasNoMatch = false;
            for (RequestDeny deny : denyList) {
                if (!deny.match(url, headers)) {
                    subHasNoMatch = true;
                    break;
                }
            }

            if (!subHasNoMatch) {
                matched = true;
                break;
            }
        }
        return matched;
    }

    @Override
    public String toString() {
        return denies.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (getClass() == obj.getClass()) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
