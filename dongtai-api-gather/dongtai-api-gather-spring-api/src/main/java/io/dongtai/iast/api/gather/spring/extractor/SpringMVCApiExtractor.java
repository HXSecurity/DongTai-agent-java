package io.dongtai.iast.api.gather.spring.extractor;

import io.dongtai.iast.api.gather.spring.convertor.RequestMappingHandlerMappingConvertor;
import io.dongtai.iast.api.openapi.domain.OpenApi;
import io.dongtai.iast.common.utils.ExceptionUtil;
import io.dongtai.log.DongTaiLog;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
    public static List<OpenApi> run(Object applicationContext) {
        WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
        SpringMVCApiExtractor springApplicationContext = new SpringMVCApiExtractor();
        List<RequestMappingHandlerMapping> requestMappingHandlerMappingList = springApplicationContext.findRequestMappingHandlerMapping(webApplicationContext);
        if (requestMappingHandlerMappingList == null || requestMappingHandlerMappingList.isEmpty()) {
            DongTaiLog.debug("spring mvc can not find RequestMappingHandlerMapping beans");
            return null;
        }
        return requestMappingHandlerMappingList
                .stream()
                .map(mapping -> {
                    try {
                        return new RequestMappingHandlerMappingConvertor(webApplicationContext, mapping).parse();
                    } catch (Throwable e) {
                        DongTaiLog.debug("spring mvc RequestMappingHandlerMappingConvertor parse error", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 从WebApplicationContext找到RequestMappingHandlerMapping
     *
     * @param applicationContext
     * @return
     */
    private List<RequestMappingHandlerMapping> findRequestMappingHandlerMapping(WebApplicationContext applicationContext) {

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
                    return Collections.singletonList(requestMappingHandlerMapping);
                }
            }
        } catch (Throwable e) {
            // 仅在出现预期外错误的时候才打印日志
            String s = ExceptionUtil.getPrintStackTraceString(e);
            if (!s.contains("java.lang.NoSuchMethodException: org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors()")) {
                DongTaiLog.debug("try use BeanFactoryUtils throw RequestMappingHandlerMapping exception", e);
            }
        }

        // 没有工具类，就只从自己里面找
        // 2023-7-11 16:58:23 注意，此处可能会寻找到多个，寻找到多个的时候统统上报
        // case:
        //         <dependency>
        //            <groupId>io.springfox</groupId>
        //            <artifactId>springfox-swagger2</artifactId>
        //            <version>2.7.0</version>
        //        </dependency>
        // 它会创建一个自己的 springfox.documentation.spring.web.PropertySourcedRequestMappingHandlerMapping 继承了 RequestMappingHandlerMapping
        return new ArrayList<>(applicationContext.getBeansOfType(RequestMappingHandlerMapping.class).values());
    }

}
