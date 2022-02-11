package io.dongtai.iast.core.bytecode.enhance.plugin.technology;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import io.dongtai.log.DongTaiLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据类名判断使用的技术
 *
 * @author dongzhiyong@huoxian.cn
 */
final public class DispatchTechnologyPlugin implements DispatchPlugin {

    private final Map<String, Map<String, String>> technologyMap;
    private String classname;

    public DispatchTechnologyPlugin() {
        this.technologyMap = new HashMap<String, Map<String, String>>();
        initTechnology();
    }


    private void initTechnology() {
        addTechnology(" org/springframework/web/servlet/DispatcherServlet".substring(1), "doService", "Spring MVC");
        addTechnology(" org/apache/struts2/dispatcher/FilterDispatcher".substring(1), "doFilter", "Struts2");
        addTechnology(" org/apache/tapestry5/TapestryFilter".substring(1), "doFilter", "Tapestry");
        addTechnology(" org/apache/wicket/protocol/http/WicketFilter".substring(1), "doFilter", "Wicket");
        addTechnology(" org/apache/struts/action/ActionServlet".substring(1), "process", "Struts");
        addTechnology(" org/apache/ecs/GenericElement".substring(1), "toString", "ECS");
        addTechnology(" org/apache/velocity/Template".substring(1), "merge", "Velocity");
        addTechnology(" org/apache/shiro/web/servlet/ShiroFilter".substring(1), "doFilter", "Shiro");
        addTechnology(" org/apache/shiro/web/servlet/IniShiroFilter".substring(1), "doFilter", "Shiro");
        addTechnology(" com/google/gwt/user/server/rpc/RemoteServiceServlet".substring(1), "processCall", "GWT");
        addTechnology(" org/hibernate/internal/SessionFactoryImpl".substring(1), "openSession", "Hibernate");
        addTechnology(" org/hibernate/internal/SessionFactoryImpl".substring(1), "openStatelessSession", "Hibernate");
        addTechnology(" org/owasp/esapi/ESAPI".substring(1), "validator", "ESAPI");
        addTechnology(" org/owasp/esapi/ESAPI".substring(1), "encoder", "ESAPI");
        addTechnology(" org/owasp/esapi/ESAPI".substring(1), "authenticator", "ESAPI");
        addTechnology(" org/owasp/esapi/ESAPI".substring(1), "accessController", "ESAPI");
        addTechnology(" org/owasp/esapi/ESAPI".substring(1), "randomizer", "ESAPI");
        addTechnology(" freemarker/template/Template".substring(1), "process", "Freemarker");
        addTechnology(" com/github/mustachejava/codes/ValueCode".substring(1), "execute", "Mustache");
        addTechnology(" com/opensymphony/module/sitemesh/parser/PageRequest".substring(1), "<init>", "Sitemesh");
        addTechnology(" javax/servlet/jsp/PageContext".substring(1), "<init>", "JSP");
        addTechnology(" coldfusion/runtime/CfJspPage".substring(1), "<init>", "CFML");
        addTechnology(" flex/messaging/MessageBrokerServlet".substring(1), "service", "BlazeDS");
        addTechnology(" coldfusion/tagext/search/SolrUtils".substring(1), "getSearchResult", "SOLR");
        addTechnology(" play/mvc/Http$RequestHeader".substring(1), "<init>", "Play MVC");
        addTechnology(" play/templates/BaseScalaTemplate".substring(1), "<init>", "Play Templates");
        addTechnology(" com/avaje/ebean/Ebean$ServerManager".substring(1), "getPrimaryServer", "Ebean");
        addTechnology(" io/netty/channel/DefaultChannelPipeline".substring(1), "<init>", "netty");
        addTechnology(" org/glassfish/jersey/server/ServerRuntime".substring(1), "process", "Jersey");
        addTechnology(" io/vertx/core/net/impl/VertxHandler".substring(1), "channelRead", "Vert.x");
    }

    private void addTechnology(String className, String methodName, String appName) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(methodName, appName);
        this.technologyMap.put(className, hashMap);
    }

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        classname = context.getClassName();
        String matchClassName = isMatch();
        if (matchClassName != null) {
            DongTaiLog.debug("current class {} hit rule \"Technology\"", classname);
            context.setMatchClassName(matchClassName);
            classVisitor = new ClassVisit(classVisitor, context, this.technologyMap.get(matchClassName));
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        if (technologyMap.containsKey(classname)) {
            return classname;
        }
        return null;
    }

    public static class ClassVisit extends AbstractClassVisitor {
        private final Map<String, String> technologyMapDetail;

        ClassVisit(ClassVisitor classVisitor, IastContext context, Map<String, String> technologyMapDetail) {
            super(classVisitor, context);
            this.technologyMapDetail = technologyMapDetail;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            String technologyName = this.technologyMapDetail.get(name);
            if (technologyName != null) {
                // todo: 后续增加应用保存和发送回服务器
                DongTaiLog.debug("discover {} technology", technologyName);
            }
            return mv;
        }

        @Override
        public boolean hasTransformed() {
            return transformed;
        }
    }
}
