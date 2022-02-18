package io.dongtai.iast.core.handler.hookpoint.models;

import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 转换hook策略为RuleModel对象
 * todo 修改hook规则模块
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IastHookRuleModel {
    private final HashMap<String, IastSinkModel> sinks;
    private final HashMap<String, IastPropagatorModel> propagators;
    private final HashMap<String, String> interfaces;
    private final HashMap<String, String> hooks = new HashMap<String, String>();

    private final HashSet<String> suffixHookPoints;
    private final HashSet<String> hookClassnames;
    private final HashSet<String> superClassHookPoints;
    private final HashMap<String, Integer> hooksValue;
    private static IastHookRuleModel instance;

    private IastHookRuleModel(HashSet<String> sources,
                              HashMap<String, IastSinkModel> sinks,
                              HashMap<String, IastPropagatorModel> propagators,
                              HashMap<String, List<String>> filters,
                              HashMap<String, String> interfaces,
                              HashMap<String, String> classs,
                              HashMap<Pattern, String> prefixHooks,
                              HashSet<String> hookClassnames,
                              HashSet<String> superClassHookPoints,
                              HashSet<String> suffixHookPoints,
                              HashMap<String, Integer> hooksValue) {
        this.sinks = sinks;
        this.propagators = propagators;
        this.interfaces = interfaces;
        this.hooks.putAll(interfaces);
        this.hooks.putAll(classs);
        this.suffixHookPoints = new HashSet<>();
        this.suffixHookPoints.add(".dubbo.monitor.support.MonitorFilter");
        this.suffixHookPoints.addAll(suffixHookPoints);
        this.hookClassnames = new HashSet<String>();
        this.hookClassnames.add(" javax.servlet.Filter".substring(1));
        this.hookClassnames.add(" javax.servlet.FilterChain".substring(1));
        this.hookClassnames.add(" javax.servlet.http.HttpServlet".substring(1));
        this.hookClassnames.add(" jakarta.servlet.http.HttpServlet".substring(1));
        this.hookClassnames.add(" javax.faces.webapp.FacesServlet".substring(1));
        this.hookClassnames.add(" javax.servlet.jsp.JspPage".substring(1));
        this.hookClassnames.add(" org.apache.jasper.runtime.HttpJspBase".substring(1));
        this.hookClassnames.add(" org.springframework.web.servlet.FrameworkServlet".substring(1));
        this.hookClassnames.add(" javax.servlet.http.Cookie".substring(1));
        this.hookClassnames.add(" org/springframework/web/servlet/mvc/annotation/AnnotationMethodHandlerAdapter$ServletHandlerMethodInvoker".substring(1));
        this.hookClassnames.addAll(hookClassnames);
        this.superClassHookPoints = superClassHookPoints;
        this.hooksValue = hooksValue;
    }

    /**
     * build HOOK点的model，优先通过云端构建，如果构建失败，从本地配置文件构建
     */
    public static void buildModel() {
        ModelBuilder.buildRemote();
    }

    public static IastHookRuleModel getInstance() {
        return instance;
    }

    public boolean isHookClass(String className) {
        return hookClassnames.contains(className) || superClassHookPoints.contains(className) || hookBySuffix(className);
    }

    /**
     * 创建IASTHook规则单例对象
     *
     * @param sources             污点来源规则
     * @param sinks               漏洞触发位置规则
     * @param propagators         污点传播规则
     * @param filters             过滤方法规则
     * @param interfaces          规则中的接口列表
     * @param classs              规则中的类列表
     * @param hookClassnames      规则中的类名
     * @param prefixHookClassName 规则的包名列表
     * @param hooksValue          hook类型及其值
     */
    private static void createInstance(HashSet<String> sources,
                                       HashMap<String, IastSinkModel> sinks,
                                       HashMap<String, IastPropagatorModel> propagators,
                                       HashMap<String, List<String>> filters,
                                       HashMap<String, String> interfaces,
                                       HashMap<String, String> classs,
                                       HashSet<String> hookClassnames,
                                       HashSet<String> hookSuperClassnames,
                                       HashSet<String> prefixHookClassName,
                                       HashMap<String, Integer> hooksValue) {
        if (instance == null) {
            Asserts.NOT_NULL("rule.source", sources);
            Asserts.NOT_NULL("rule.sink", sinks);
            Asserts.NOT_NULL("rule.propagator", propagators);
            Asserts.NOT_NULL("rule.filter", filters);
            Asserts.NOT_NULL("rule.interface", interfaces);
            Asserts.NOT_NULL("rule.class", classs);
            Asserts.NOT_NULL("rule.classname", hookClassnames);
            Asserts.NOT_NULL("rule.hook.values", hooksValue);
            instance = new IastHookRuleModel(sources, sinks, propagators, filters, interfaces, classs, ModelBuilder.PREFIX_HOOKS, hookClassnames, hookSuperClassnames, prefixHookClassName, hooksValue);
        }
    }

    /**
     * 获取方法签名对应的辅助传播方法对象
     *
     * @param signature 方法签名
     * @return 方法对应的传播节点对象
     */
    public static IastPropagatorModel getPropagatorByMethodSignature(String signature) {
        if (instance != null) {
            return instance.propagators.get(signature);
        } else {
            return null;
        }
    }

    /**
     * 获取方法签名对应的sink方法对象
     *
     * @param signature 方法签名
     * @return sink对象
     */
    public static IastSinkModel getSinkByMethodSignature(String signature) {
        if (instance != null) {
            return instance.sinks.get(signature);
        } else {
            return null;
        }
    }

    /**
     * 根据类名判断类是否需要被HOOK（AOP），类名支持：
     * - 全称，如：java/lang/String
     * - 前缀，如：com/mysql/
     *
     * @param classname 全限定类名，如：java/lang/String
     * @return 是否需要被HOOK
     */
    public static boolean hookByName(String classname) {
        if (instance != null) {
            return instance.hookClassnames.contains(classname) || hookBySuffix(classname);
        } else {
            return false;
        }
    }

    /**
     * 根据父类（接口）名称判断类是否需要被HOOK（AOP），支持：
     * - 全称，如：java/lang/String
     * <p>
     * 该方法不支持正则匹配，针对父类或接口的匹配必须使用全限定类名
     *
     * @param classname 全限定类名，如：java/lang/String
     * @return 是否需要被HOOK
     */
    public static boolean hookBySuperClass(String classname) {
        if (instance != null) {
            return instance.superClassHookPoints.contains(classname);
        } else {
            return false;
        }
    }

    /**
     * 检查当前类是否命中通过前缀匹配
     *
     * @param classname 根据前缀检测类是否需要hook
     * @return true-需要hook，false-不需要hook
     */
    private static boolean hookBySuffix(String classname) {
        for (String suffix : instance.suffixHookPoints) {
            if (classname.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取方法签名对应的框架名称，用于判断方法所属的HOOK点类型，如；String、IO等
     * fixme: 临时关闭正则匹配的方式
     *
     * @param signature 方法签名
     * @return 框架名，如：String、sql、ldap等
     */
    public static String getFrameworkByMethodSignature(String signature) {
        if (instance != null) {
            return instance.hooks.get(signature);
        } else {
            return null;
        }
    }

    /**
     * 获取框架对应的HOOK规则值
     * - pro值为
     * - sink类型的值为
     *
     * @param framework 框架名称
     * @return hook规则类型值
     */
    public static int getRuleTypeValueByFramework(String framework) {
        if (instance != null) {
            try {
                return instance.hooksValue.get(framework);
            } catch (java.lang.NullPointerException e) {
                //规则待更新
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * 通过内部类转换XML规则为IAST的模型规则
     */
    private static class ModelBuilder {
        // 将class和interface合并
        private final static HashMap<String, String> SUPER_CLASS_HOOKS = new HashMap<String, String>();
        private final static HashMap<String, String> CLASS_HOOKS = new HashMap<String, String>();
        private final static HashSet<String> CLASSNAME_HOOKS = new HashSet<String>();
        private final static HashSet<String> SUPER_CLASSNAME_HOOKS = new HashSet<String>();
        private final static HashSet<String> PREFIX_CLASS_HOOKS = new HashSet<String>();
        private final static HashMap<Pattern, String> PREFIX_HOOKS = new HashMap<Pattern, String>();

        /**
         * 从云端获取hook规则并进行xxx
         */
        public static void buildRemote() {
            JSONArray rules = loadRemoteRule();
            if (rules != null) {
                buildRuleModel(rules);
            }
        }

        private static JSONArray loadRemoteRule() {
            StringBuilder respRaw = HttpClientUtils.sendGet(Constants.API_HOOK_PROFILE, null, null);
            if (respRaw != null) {
                JSONObject resp = new JSONObject(respRaw.toString());
                return resp.getJSONArray("data");
            }
            return null;
        }

        private static void buildRuleModel(JSONArray rules) {
            HashMap<String, IastSinkModel> sinks = new HashMap<String, IastSinkModel>(500);
            HashMap<String, IastPropagatorModel> propagators = new HashMap<String, IastPropagatorModel>(500);
            HashSet<String> sources = new HashSet<String>(500);
            HashMap<String, List<String>> filters = new HashMap<String, List<String>>(500);
            HashMap<String, Integer> hooksValue = new HashMap<String, Integer>(10);

            JSONObject rule, detail, errorLogReport;
            JSONArray details;
            Object sourcePos, targetPos;
            String value, ruleItemSource, ruleItemTarget, ruleItemValue, ruleItemTrack, ruleItemInherit;
            IastPropagatorModel propagator;
            IastSinkModel sink;
            for (int i = 0, rulesLength = rules.length(); i < rulesLength; i++) {
                rule = rules.getJSONObject(i);
                value = rule.getString("value");
                int type = rule.getInt("type");
                details = rule.getJSONArray("details");


                for (int j = 0, detailsLength = details.length(); j < detailsLength; j++) {
                    detail = details.getJSONObject(j);
                    ruleItemSource = detail.getString("source");
                    ruleItemTarget = detail.getString("target");
                    ruleItemValue = detail.getString("value");
                    ruleItemTrack = detail.getString("track");
                    ruleItemInherit = detail.getString("inherit");
                    try {
                        initHookPoint(ruleItemInherit, ruleItemValue, value);

                        switch (type) {
                            case 1:
                                hooksValue.put(value, HookType.PROPAGATOR.getValue());
                                sourcePos = convertLink2Object(ruleItemSource);
                                targetPos = convertLink2Object(ruleItemTarget);
                                propagator = new IastPropagatorModel(value, value, ruleItemSource, sourcePos, ruleItemTarget, targetPos);
                                propagators.put(ruleItemValue, propagator);
                                break;
                            case 2:
                                hooksValue.put(value, HookType.SOURCE.getValue());
                                sources.add(ruleItemValue);
                                break;
                            case 3:
                                hooksValue.put(value, HookType.PROPAGATOR.getValue());

                                sourcePos = convertLink2Object(ruleItemSource);
                                targetPos = convertLink2Object(ruleItemTarget);
                                propagator = new IastPropagatorModel(value, value, ruleItemSource, sourcePos, ruleItemTarget, targetPos);
                                propagators.put(ruleItemValue, propagator);

                                List<String> filterList;
                                if (filters.containsKey(value)) {
                                    filterList = filters.get(value);
                                } else {
                                    filterList = new ArrayList<String>();
                                    filters.put(value, filterList);
                                }
                                filterList.add(ruleItemValue);
                                break;
                            case 4:
                                hooksValue.put(value, HookType.SINK.getValue());
                                int[] intPositions;
                                if (ruleItemSource.startsWith("P")) {
                                    intPositions = StringUtils.convertStringToIntArray(ruleItemSource);
                                } else {
                                    intPositions = null;
                                }
                                sink = new IastSinkModel(ruleItemValue, value, intPositions, ruleItemTrack);
                                sinks.put(ruleItemValue, sink);
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        errorLogReport = new JSONObject();
                        errorLogReport.put("type", "rule");
                        errorLogReport.put("rule", details.toString());
                        errorLogReport.put("msg", ThrowableUtils.getStackTrace(e));
                        ErrorLogReport.sendErrorLog(errorLogReport.toString());
                    }
                }
            }
            IastHookRuleModel.createInstance(sources, sinks, propagators, filters, SUPER_CLASS_HOOKS, CLASS_HOOKS, CLASSNAME_HOOKS, SUPER_CLASSNAME_HOOKS, PREFIX_CLASS_HOOKS, hooksValue);
        }

        /**
         * 初始化hook点配置
         *
         * @param inherit   是否继承
         * @param signature 方法签名
         * @param type      方法类型
         */
        private static void initHookPoint(String inherit, String signature, String type) {
            String signatureNoArg = signature.substring(0, signature.indexOf('('));
            String classname = signatureNoArg.substring(0, signatureNoArg.lastIndexOf('.'));
            boolean isReMatch = false;

            // todo 后续考虑增加通配符型规则
            if ("true".equals(inherit)) {
                SUPER_CLASSNAME_HOOKS.add(classname);
                SUPER_CLASS_HOOKS.put(signature, type);
            } else if ("all".equals(inherit)) {
                SUPER_CLASSNAME_HOOKS.add(classname);
                SUPER_CLASS_HOOKS.put(signature, type);

                CLASSNAME_HOOKS.add(classname);
                CLASS_HOOKS.put(signature, type);
            } else {
                if (isReMatch) {
                    PREFIX_HOOKS.put(Pattern.compile(signature), type);
                } else {
                    CLASSNAME_HOOKS.add(classname);
                    CLASS_HOOKS.put(signature, type);
                }
            }
        }

        private static Object convertLink2Object(String link) {
            Object objLink = null;
            // 如果已P开头且不含&或|
            if (link.startsWith("P")) {
                objLink = StringUtils.convertStringToIntArray(link);
            } else if (link.contains("&")) {
                String[] links = link.split("&");
                for (String s : links) {
                    if (s.startsWith("P")) {
                        objLink = StringUtils.convertStringToIntArray(s);
                    }
                }
            } else if (link.contains("|")) {
                String[] links = link.split("\\|");
                for (String s : links) {
                    if (s.startsWith("P")) {
                        objLink = StringUtils.convertStringToIntArray(s);
                    }
                }
            } else {
                objLink = link;
            }
            return objLink;
        }
    }
}
