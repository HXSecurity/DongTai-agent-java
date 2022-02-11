package io.dongtai.iast.core.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class HttpClientHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
