package io.dongtai.iast.api.gather.spring.extractor;

import io.dongtai.iast.api.gather.spring.convertor.RequestMappingHandlerMappingConvertor;
import io.dongtai.iast.api.openapi.domain.OpenApi;
import io.dongtai.log.DongTaiLog;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author CC11001100
 * @since v1.12.0
 */
public class SpringMVCApiExtractor {

    /**
     * 从传递的webApplicationContext中收集api地址
     *
     * @param applicationContext
     * @return
     */
    public static OpenApi run(Object applicationContext) {
        WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
        SpringMVCApiExtractor springApplicationContext = new SpringMVCApiExtractor();
        RequestMappingHandlerMapping requestMappingHandlerMapping = springApplicationContext.findRequestMappingHandlerMapping(webApplicationContext);
        return new RequestMappingHandlerMappingConvertor(webApplicationContext, requestMappingHandlerMapping).parse();
    }

    /**
     * 从WebApplicationContext找到RequestMappingHandlerMapping
     *
     * @param applicationContext
     * @return
     */
    private RequestMappingHandlerMapping findRequestMappingHandlerMapping(WebApplicationContext applicationContext) {

        if (applicationContext == null) {
            return null;
        }

        // 如果在当前类中能够找到这个util，则使用这个util
        try {
            // 以下反射代码相当于：
            // Map<String, RequestMappingHandlerMapping> requestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class, true, false);
            // RequestMappingHandlerMapping handlerMapping = requestMappings.get("requestMappingHandlerMapping");
            Class<?> clazz = Class.forName("org.springframework.beans.factory.BeanFactoryUtils");
            Method beansOfTypeIncludingAncestors = clazz.getDeclaredMethod("beansOfTypeIncludingAncestors");
            Object invoke = beansOfTypeIncludingAncestors.invoke(clazz);
            if (invoke instanceof Map) {
                Map<String, RequestMappingHandlerMapping> m = (Map<String, RequestMappingHandlerMapping>) invoke;
                RequestMappingHandlerMapping requestMappingHandlerMapping = m.get("requestMappingHandlerMapping");
                if (requestMappingHandlerMapping != null) {
                    return requestMappingHandlerMapping;
                }
            }
        } catch (Throwable e) {
            DongTaiLog.debug("try use BeanFactoryUtils find RequestMappingHandlerMapping exception", e);
        }

        // 没有工具类，就只从自己里面找
        return applicationContext.getBean(RequestMappingHandlerMapping.class);
    }

}
