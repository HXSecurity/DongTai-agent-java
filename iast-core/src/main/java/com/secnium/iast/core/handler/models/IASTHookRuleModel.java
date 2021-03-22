package com.secnium.iast.core.handler.models;

import com.secnium.iast.core.handler.controller.HookType;
import com.secnium.iast.core.util.Asserts;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 转换hook策略为RuleModel对象
 * todo 修改hook规则模块
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IASTHookRuleModel {
    private final HashMap<String, IASTSinkModel> sinks;
    private final HashMap<String, IASTPropagatorModel> propagators;
    private final HashSet<String> sources;
    private final HashMap<String, List<String>> filters;
    private final HashMap<String, String> interfaces;
    private final HashMap<String, String> classs;
    private final HashMap<String, String> hooks = new HashMap<String, String>();

    private final HashMap<Pattern, String> prefixHooks;
    private final HashSet<String> prefixHookClassName;
    private final HashSet<String> hookClassnames;
    private final HashSet<String> hookSuperClassnames;
    private HashMap<String, Integer> hooksValue;
    private static IASTHookRuleModel instance;

    private IASTHookRuleModel(HashSet<String> sources,
                              HashMap<String, IASTSinkModel> sinks,
                              HashMap<String, IASTPropagatorModel> propagators,
                              HashMap<String, List<String>> filters,
                              HashMap<String, String> interfaces,
                              HashMap<String, String> classs,
                              HashMap<Pattern, String> prefixHooks,
                              HashSet<String> hookClassnames,
                              HashSet<String> hookSuperClassnames,
                              HashSet<String> prefixHookClassName,
                              HashMap<String, Integer> hooksValue) {
        this.sources = sources;
        this.sinks = sinks;
        this.propagators = propagators;
        this.filters = filters;
        this.interfaces = interfaces;
        this.classs = classs;
        this.hooks.putAll(interfaces);
        this.hooks.putAll(classs);
        this.prefixHooks = prefixHooks;
        this.prefixHookClassName = prefixHookClassName;
        this.hookClassnames = hookClassnames;
        this.hookSuperClassnames = hookSuperClassnames;
        this.hooksValue = hooksValue;
    }

    /**
     * build HOOK点的model，优先通过云端构建，如果构建失败，从本地配置文件构建
     */
    public static void buildModel() {
        ModelBuilder.buildRemote();
    }

    /**
     * 当前方法用于单元测试
     */
    public static void buildModelRemote() {
        ModelBuilder.buildRemote();
    }

    /**
     * 创建IASTHook规则单例对象
     *
     * @param sources
     * @param sinks
     * @param propagators
     * @param filters
     * @param interfaces
     * @param classs
     * @param hookClassnames
     * @param prefixHookClassName
     * @param hooksValue
     */
    private static void createInstance(HashSet<String> sources,
                                       HashMap<String, IASTSinkModel> sinks,
                                       HashMap<String, IASTPropagatorModel> propagators,
                                       HashMap<String, List<String>> filters,
                                       HashMap<String, String> interfaces,
                                       HashMap<String, String> classs,
                                       HashMap<Pattern, String> prefixHooks,
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
            instance = new IASTHookRuleModel(sources, sinks, propagators, filters, interfaces, classs, prefixHooks, hookClassnames, hookSuperClassnames, prefixHookClassName, hooksValue);
        }
    }

    /**
     * 获取方法签名对应的辅助传播方法对象
     *
     * @param signature 方法签名
     * @return 方法对应的传播节点对象
     */
    public static IASTPropagatorModel getPropagatorByMethodSignature(String signature) {
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
    public static IASTSinkModel getSinkByMethodSignature(String signature) {
        if (instance != null) {
            return instance.sinks.get(signature);
        } else {
            return null;
        }
    }

    /**
     * 获取vulType类型漏洞对应的过滤器列表，用于在检测污点链中是否存在过滤函数
     *
     * @param vulType 漏洞类型
     * @return 过滤器列表
     */
    public static List<String> getFilterByVulType(String vulType) {
        if (instance != null) {
            return instance.filters.get(vulType);
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
    public static boolean classIsNeededHookByName(String classname) {
        if (instance != null) {
            return instance.hookClassnames.contains(classname) || classIsNeededHookByClassNamePrefix(classname);
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
    public static boolean classIsNeededHookBySuperClassName(String classname) {
        if (instance != null) {
            return instance.hookSuperClassnames.contains(classname);
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
    private static boolean classIsNeededHookByClassNamePrefix(String classname) {
        for (String prefix : instance.prefixHookClassName) {
            if (classname.startsWith(prefix)) {
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
            //String framework = instance.hooks.get(signature);
            //return framework == null ? getFrameworkByReMethodSignature(signature) : framework;
        } else {
            return null;
        }
    }

    public static String getFrameworkByReMethodSignature(String signature) {
        for (Map.Entry<Pattern, String> item : instance.prefixHooks.entrySet()) {
            Pattern p = item.getKey();
            if (p.matcher(signature).find()) {
                return item.getValue();
            }
        }
        return null;
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
            return instance.hooksValue.get(framework);
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
        private final static HashMap<Pattern, String> prefixHooks = new HashMap<Pattern, String>();

        /**
         * 从云端获取hook规则并进行xxx
         */
        public static boolean buildRemote() {
            JSONArray rules = loadRemoteRule();
            if (rules != null) {
                buildRuleModel(rules);
                return true;
            }
            return false;
        }

        private static JSONArray loadRemoteRule() {
            String respRaw = HttpClientUtils.sendGet(Constants.API_HOOK_PROFILE, null, null);
            if (respRaw != null) {
                JSONObject resp = new JSONObject(respRaw);
                return resp.getJSONArray("data");
            }
            return null;
        }

        private static void buildRuleModel(JSONArray rules) {
            HashMap<String, IASTSinkModel> sinks = new HashMap<String, IASTSinkModel>();
            HashMap<String, IASTPropagatorModel> propagators = new HashMap<String, IASTPropagatorModel>();
            HashSet<String> sources = new HashSet<String>();
            HashMap<String, List<String>> filters = new HashMap<String, List<String>>();
            HashMap<String, Integer> hooksValue = new HashMap<String, Integer>();

            for (int i = 0, rulesLength = rules.length(); i < rulesLength; i++) {
                JSONObject rule = rules.getJSONObject(i);
                String value = rule.getString("value");
                int type = rule.getInt("type");
                JSONArray details = rule.getJSONArray("details");

                IASTPropagatorModel propagator;
                for (int j = 0, detailsLength = details.length(); j < detailsLength; j++) {
                    JSONObject detail = details.getJSONObject(j);
                    String ruleItemSource = detail.getString("source");
                    String ruleItemTarget = detail.getString("target");
                    String ruleItemValue = detail.getString("value");
                    String ruleItemTrack = detail.getString("track");
                    String ruleItemInherit = detail.getString("inherit");

                    initHookPoint(ruleItemInherit, ruleItemValue, value);
                    //
                    switch (type) {
                        case 1:
                            // 处理传播节点
                            // todo create and add propagatoo node
                            hooksValue.put(value, HookType.PROPAGATOR.getValue());
                            Object sourcePos = convertLink2Object(ruleItemSource);
                            Object targetPos = convertLink2Object(ruleItemTarget);
                            propagator = new IASTPropagatorModel(value, value, ruleItemSource, sourcePos, ruleItemTarget, targetPos);
                            propagators.put(ruleItemValue, propagator);
                            break;
                        case 2:
                            hooksValue.put(value, HookType.SOURCE.getValue());
                            sources.add(ruleItemValue);
                            break;
                        case 3:
                            // 处理filter点
                            hooksValue.put(value, HookType.PROPAGATOR.getValue());
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
                            // 处理sink点
                            hooksValue.put(value, HookType.SINK.getValue());
                            int[] intPositions;
                            if (ruleItemSource.startsWith("P")) {
                                intPositions = StringUtils.convertStringToIntArray(ruleItemSource);
                            } else {
                                intPositions = null;
                            }
                            IASTSinkModel sink = new IASTSinkModel(ruleItemValue, value, intPositions, ruleItemTrack);
                            sinks.put(ruleItemValue, sink);
                            break;
                        case 5:
                            break;
                        default:
                            break;
                    }
                }
            }
            IASTHookRuleModel.createInstance(sources, sinks, propagators, filters, SUPER_CLASS_HOOKS, CLASS_HOOKS, prefixHooks, CLASSNAME_HOOKS, SUPER_CLASSNAME_HOOKS, PREFIX_CLASS_HOOKS, hooksValue);
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

            // 后续考虑增加通配符型规则
//            if (classname.endsWith("*")) {
//                isReMatch = true;
//                PREFIX_CLASS_HOOKS.add(classname.substring(0, classname.length() - 1));
//            }

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
                    prefixHooks.put(Pattern.compile(signature), type);
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
