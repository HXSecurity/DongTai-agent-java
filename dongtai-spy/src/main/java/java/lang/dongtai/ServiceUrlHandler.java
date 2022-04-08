package java.lang.dongtai;

import java.util.List;

public interface ServiceUrlHandler {
    List<ServiceUrl> processUrl(String host, String port);
}
