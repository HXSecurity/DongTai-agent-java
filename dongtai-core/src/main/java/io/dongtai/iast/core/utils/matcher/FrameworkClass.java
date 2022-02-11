package io.dongtai.iast.core.utils.matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class FrameworkClass extends AbstractMatcher {
    private static final String[] CLASSES;

    @Override
    public boolean match(String classname) {
        return StringUtils.startsWithAny(classname, CLASSES);
    }

    static {
        CLASSES = new String[]{
                "ch/qos/",
                "com/opensymphony/",
                "com/sun/",
                "freemarker/cache/",
                "freemarker/core/",
                "freemarker/ext/",
                "freemarker/template/",
                "java/security/",
                "java/util/",
                "javassist/ClassPath/",
                "javax/annotation/",
                "javax/el/",
                "javax/security/",
                "javax/servlet/",
                "javax/websocket/",
                "ognl/",
                "org/springframework/",
                "org/eclipse/",
                "org/slf4j/",
                "sun/reflect/",
                "sun/security",
                "org/hsqldb/jdbc/",
                "org/springframework/",
                "com/alibaba/",
                "com/mysql/",
                "org/quartz/",
                "com/google/",
                // 序列化框架
                "com/fasterxml/jackson/",
                "com/alibaba/fastjson/",
                // YAML
                "org/yaml/snakeyaml/",
                // 时间处理框架
                "org/joda/time/",
                // ORM框架
                "org/hibernate/",
                "org/mybatis/",
                "org/apache/ibatis/",
                // Validate-api
                "javax/validation/",
                // javaFX
                "javafx/",
                // 安全框架
                "org/apache/shiro/",
                "com/jagregory/shiro/",
                // API框架
                "springfox/",
                // JDBC and DB
                "com/mysql/",
                "com/alibaba/druid/",
                // 模板引擎
                "freemarker/",
                // commons-fileupload
                "org/apache/commons/fileupload/",
                // Hutool
                "cn/hutool/",
                // google-collection
                "com/google/common/collect/",
                // fasterxml
                "com/fasterxml/classmate/",
                // Lettuce
                "io/lettuce/",
                // neety
                "io/netty/",
                // pagehelper
                "com/github/pagehelper/",
                // AspectJ
                "org/aspectj/",
                // EL
                "org/apache/el/",
                // HDR https://github.com/HdrHistogram/HdrHistogram
                "org/HdrHistogram/",
                // reactor 反应式编程框架 https://projectreactor.io/
                "reactor/",
                // 实体映射工具
                "org/mapstruct/",
                // nosql
                "redis/",
                //  图片处理框架
                "com/github/jaiimageio/",
                // sql解析工具
                "net/sf/jsqlparser/",
        };
    }
}
