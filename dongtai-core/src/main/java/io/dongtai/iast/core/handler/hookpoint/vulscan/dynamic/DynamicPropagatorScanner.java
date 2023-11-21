package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNodeType;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;
import io.dongtai.iast.core.handler.hookpoint.service.trace.HttpService;
import io.dongtai.iast.core.handler.hookpoint.service.trace.ServiceTrace;
import io.dongtai.iast.core.handler.hookpoint.vulscan.IVulScan;
import io.dongtai.iast.core.handler.hookpoint.vulscan.VulnType;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe.XXECheck;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;

import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DynamicPropagatorScanner implements IVulScan {
    private final static Set<SinkSafeChecker> SAFE_CHECKERS = new HashSet<>(Arrays.asList(
            new FastjsonCheck(),
            new XXECheck(),
            new QLExpressCheck()
    ));

    private final static Set<SinkSourceChecker> SOURCE_CHECKERS = new HashSet<>(Arrays.asList(
            new PathTraversalCheck(),
            new SSRFSourceCheck(),
            new UnvalidatedRedirectCheck()
    ));

    private static final Set<ServiceTrace> SERVICE_TRACES = new HashSet<>(Collections.singletonList(
            new HttpService()
    ));

    // VulnType => List<TAGS, UNTAGS>
    private static final Map<String, List<TaintTag[]>> TAINT_TAG_CHECKS = new HashMap<String, List<TaintTag[]>>() {{
        put(VulnType.REFLECTED_XSS.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED, TaintTag.CROSS_SITE},
                new TaintTag[]{TaintTag.BASE64_ENCODED, TaintTag.HTML_ENCODED, TaintTag.LDAP_ENCODED,
                        TaintTag.SQL_ENCODED, TaintTag.URL_ENCODED, TaintTag.XML_ENCODED, TaintTag.XPATH_ENCODED,
                        TaintTag.XSS_ENCODED, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.SQL_INJECTION.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.SQL_ENCODED, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.HQL_INJECTION.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.SQL_ENCODED, TaintTag.CUSTOM_ENCODED_HQL_INJECTION,
                        TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.LDAP_INJECTION.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.BASE64_ENCODED, TaintTag.HTML_ENCODED, TaintTag.LDAP_ENCODED,
                        TaintTag.SQL_ENCODED, TaintTag.URL_ENCODED, TaintTag.XML_ENCODED, TaintTag.XPATH_ENCODED,
                        TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.XPATH_INJECTION.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.XML_ENCODED, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.CMD_INJECTION.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.BASE64_ENCODED, TaintTag.HTML_ENCODED, TaintTag.LDAP_ENCODED,
                        TaintTag.SQL_ENCODED, TaintTag.URL_ENCODED, TaintTag.XML_ENCODED, TaintTag.XPATH_ENCODED,
                        TaintTag.CUSTOM_ENCODED_CMD_INJECTION,TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.PATH_TRAVERSAL.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.BASE64_ENCODED, TaintTag.HTML_ENCODED, TaintTag.LDAP_ENCODED,
                        TaintTag.URL_ENCODED, TaintTag.XML_ENCODED, TaintTag.XPATH_ENCODED,
                        TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.UNVALIDATED_REDIRECT.getName(), Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.URL_ENCODED, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.XXE.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_XXE, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.JNDI_INJECTION.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_JNDI_INJECTION, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.NOSQL_INJECTION.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_NOSQL_INJECTION, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.SMTP_INJECTION.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_SMTP_INJECTION, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.EL_INJECTION.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_EL_INJECTION, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.REFLECTION_INJECTION.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_REFLECTION_INJECTION, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.SSRF.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_XXE, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.FILE_WRITE.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_FILE_WRITE, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
        put(VulnType.REDOS.getName(),Arrays.asList(
                new TaintTag[]{TaintTag.UNTRUSTED},
                new TaintTag[]{TaintTag.CUSTOM_ENCODED_REDOS, TaintTag.HTTP_TOKEN_LIMITED_CHARS, TaintTag.NUMERIC_LIMITED_CHARS}
        ));
    }};

    @Override
    public void scan(MethodEvent event, SinkNode sinkNode) {
        for (SinkSafeChecker chk : SAFE_CHECKERS) {
            if (chk.match(event, sinkNode) && chk.isSafe(event, sinkNode)) {
                return;
            }
        }

        if (!HttpService.validate(event)) {
            return;
        }

        boolean serviceCall = false;
        for (ServiceTrace serviceTrace : SERVICE_TRACES) {
            if (serviceTrace.match(event, sinkNode)) {
                serviceCall = true;
                serviceTrace.addTrace(event, sinkNode);
            }
        }

        boolean hit = sinkSourceHitTaintPool(event, sinkNode);
        if (serviceCall || hit) {
            StackTraceElement[] stackTraceElements = StackUtils.createCallStack(5);
            if (sinkNode.hasDenyStack(stackTraceElements)) {
                return;
            }
            event.setCallStacks(stackTraceElements);
            int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
            event.setInvokeId(invokeId);
            event.setPolicyType(PolicyNodeType.SINK.getName());
            event.setTaintPositions(sinkNode.getSources(), null);
            event.setStacks(stackTraceElements);

            EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
        }
    }

    /**
     * sink方法的污点来源是否命中污点池，用于过滤未命中污点池的sink方法，避免浪费资源，设置污点源去向
     *
     * @param event    current method event
     * @param sinkNode current sink policy node
     * @return 当前方法是否命中污点池
     */
    private boolean sinkSourceHitTaintPool(MethodEvent event, SinkNode sinkNode) {
        for (SinkSourceChecker chk : SOURCE_CHECKERS) {
            if (chk.match(event, sinkNode)) {
                return chk.checkSource(event, sinkNode);
            }
        }

        List<Object> sourceInstances = new ArrayList<>();
        boolean hasTaint = false;
        boolean objHasTaint = false;
        Set<TaintPosition> sources = sinkNode.getSources();
        for (TaintPosition position : sources) {
            if (position.isObject()) {
                if (TaintPoolUtils.isNotEmpty(event.objectInstance)
                        && TaintPoolUtils.isAllowTaintType(event.objectInstance)
                        && TaintPoolUtils.poolContains(event.objectInstance, event)) {
                    objHasTaint = true;
                    hasTaint = true;
                    addSourceInstance(sourceInstances, event.objectInstance);
                }
            } else if (position.isParameter()) {
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= event.parameterInstances.length) {
                    continue;
                }
                boolean paramHasTaint = false;
                Object parameter = event.parameterInstances[parameterIndex];
                if (TaintPoolUtils.isNotEmpty(parameter)
                        && TaintPoolUtils.isAllowTaintType(parameter)
                        && TaintPoolUtils.poolContains(parameter, event)) {
                    paramHasTaint = true;
                    hasTaint = true;
                    addSourceInstance(sourceInstances, parameter);
                }
                event.addParameterValue(parameterIndex, parameter, paramHasTaint);
            }
        }


        if (!sourceInstances.isEmpty()) {
            List<TaintTag[]> tagList = TAINT_TAG_CHECKS.get(sinkNode.getVulType());
            if (tagList != null) {
                boolean tagsHit = false;
                TaintTag[] required = tagList.get(0);
                TaintTag[] disallowed = tagList.get(1);

                for (Object sourceInstance : sourceInstances) {
                    long hash = TaintPoolUtils.getStringHash(sourceInstance);
                    TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(hash);
                    if (tr == null || tr.isEmpty()) {
                        continue;
                    }
                    
                    boolean commonCondition = tr.hasRequiredTaintTags(required) && !tr.hasDisallowedTaintTags(disallowed);

                    if (PropertyUtils.validatedSink()) {
                        tagsHit = commonCondition && !tr.hasValidatedTags(disallowed);
                    } else {
                        tagsHit = commonCondition;
                    }
                }
                if (!tagsHit) {
                    return false;
                }
            }
        }

        if (hasTaint) {
            event.setObjectValue(event.objectInstance, objHasTaint);
        }

        return hasTaint;
    }

    private void addSourceInstance(List<Object> sourceInstances, Object obj) {
        if (obj instanceof String[]) {
            String[] stringArray = (String[]) obj;
            for (String stringItem : stringArray) {
                addSourceInstance(sourceInstances, stringItem);
            }
        } else if (obj instanceof Object[]) {
            Object[] objArray = (Object[]) obj;
            for (Object objItem : objArray) {
                addSourceInstance(sourceInstances, objItem);
            }
        } else {
            sourceInstances.add(obj);
        }
    }
}
