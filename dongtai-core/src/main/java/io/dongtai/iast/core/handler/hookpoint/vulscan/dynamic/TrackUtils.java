package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class TrackUtils {
    private static boolean isMatch(Object source, Object target) {
        if (isEmpty(target, false) || isEmpty(source, false)) {
            return false;
        }

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        if (sourceClass.isArray() && !sourceClass.getComponentType().isPrimitive()) {
            // 遍历非基本类型的Array
            Object[] arrayOfSources = (Object[]) source;
            for (Object arrayOfSource : arrayOfSources) {
                if (arrayOfSource != null) {
                    if (isMatch(arrayOfSource, target)) {
                        return true;
                    }
                }
            }
        } else if (targetClass.isArray() && !targetClass.getComponentType().isPrimitive()) {
            // 遍历非基本类型的Array
            Object[] arrayOfSources = (Object[]) target;
            for (Object arrayOfSource : arrayOfSources) {
                if (arrayOfSource != null) {
                    if (isMatch(source, arrayOfSource)) {
                        return true;
                    }
                }
            }
        } else if (sourceClass.equals(String.class) && targetClass.equals(String.class)) {
            return matchString((String) source, (String) target);
        } else if (sourceClass.equals(targetClass)) {
            return source.hashCode() == target.hashCode();
        }

        return false;
    }

    private static boolean matchString(String source, String target) {
        return StringUtils.match(source, target);
    }

    /**
     * 检查污点数据是否为空
     *
     * @param taintValue 污点值
     * @param isSink     是否为sink方法调用
     * @return true-污点为空，false-污点不为空
     */
    public static boolean isEmpty(Object taintValue, boolean isSink) {
        boolean empty = false;
        if (taintValue == null) {
            empty = true;
        } else {
            Class<?> taintClass = taintValue.getClass();
            if (taintValue instanceof List) {
                if (((List<?>) taintValue).isEmpty()) {
                    empty = true;
                }
            } else if (taintValue instanceof Collection) {
                if (((Collection<?>) taintValue).isEmpty()) {
                    empty = true;
                }
            } else if (taintClass.isArray() && !taintClass.getComponentType().isPrimitive()) {
                boolean isNotEmpty = false;
                Object[] taintValues = (Object[]) taintValue;
                for (Object value : taintValues) {
                    if (null != value) {
                        if (!isEmpty(value, isSink)) {
                            isNotEmpty = true;
                        }
                    }
                }
                empty = !isNotEmpty;
            } else if (taintValue instanceof Map) {
                if (((Map<?, ?>) taintValue).isEmpty()) {
                    empty = true;
                }
            } else if (taintValue instanceof String) {
                if (((String) taintValue).isEmpty()) {
                    empty = true;
                }
            } else if (taintValue instanceof StringBuilder) {
                if (((StringBuilder) taintValue).toString().isEmpty()) {
                    empty = true;
                }

            }
        }
        return empty;
    }

    public static boolean smartEventMatchAndSetTaint(Object taintValue, MethodEvent event) {
        boolean matchStatus = false;
        // 如果方法不是source节点
        if (event.leave) {
            // 方法已执行完成, taintValue是当前污点
            if (isMatch(taintValue, event.returnValue)) {
                // 污点与方法返回值相同
                if (isMatch(taintValue, event.object)) {
                    // 污点与方法返回值相同，本身相同
                    boolean matchArg = false;
                    Object[] argTaint = new Object[event.argumentArray.length];
                    // 如果参数存在，进入匹配环节，否则丢弃堆栈
                    int taintIndex = 0;
                    for (Object arg : event.argumentArray) {
                        boolean isAllStringMatch = true;
                        if (arg instanceof String && event.returnValue instanceof String) {
                            String enhanceArg = (String) arg;
                            String enhanceReture = (String) event.returnValue;
                            isAllStringMatch = enhanceArg.contains(enhanceReture) || enhanceReture.contains(enhanceArg);
                        }
                        if (isAllStringMatch) {
                            if (isMatch(taintValue, arg)) {
                                // // 污点与方法返回值相同，本身相同，参数相同
                                argTaint[taintIndex] = arg;
                                matchArg = true;
                                taintIndex++;
                            }
                        }
                    }

                    if (!matchArg) {
                        // 污点与方法返回值相同，本身相同，参数不同
                        event.inValue = event.object;
                        matchStatus = true;
                    } else {
                        event.inValue = argTaint;
                        matchStatus = true;
                    }
                } else {
                    // 污点与方法返回值相同，本身不同
                    boolean matchArg = false;
                    Object[] argTaint = new Object[event.argumentArray.length];
                    // 污点与方法返回值相同、本身不同、参数相同
                    // 如果参数存在，进入匹配环节，否则丢弃堆栈
                    int taintIndex = 0;
                    for (Object arg : event.argumentArray) {
                        boolean isAllStringMatch = true;
                        if (arg instanceof String && event.returnValue instanceof String) {
                            String enhanceArg = (String) arg;
                            String enhanceReture = (String) event.returnValue;
                            isAllStringMatch = enhanceArg.contains(enhanceReture) || enhanceReture.contains(enhanceArg);
                        }
                        if (isAllStringMatch) {
                            if (isMatch(taintValue, arg)) {
                                // // 污点与方法返回值相同，本身相同，参数相同
                                argTaint[taintIndex] = arg;
                                matchArg = true;
                                taintIndex++;
                            }
                        }
                    }

                    if (matchArg) {
                        // 污点与方法返回值相同、本身不同、参数相同，传播值本身，参数
                        event.inValue = argTaint;
                        matchStatus = true;
                    } else {
                        // 如果方法没有参数，设置污点为对象；
                        if (event.argumentArray.length == 0) {
                            event.inValue = event.object;
                        } /*else if (event.isStatic) {
                            //  如果方法有参数，设置污点为参数
                            event.inValue = event.argumentArray;
                        }*/
                        matchStatus = true;
                    }
                }
                if (matchStatus) {
                    event.outValue = event.returnValue;
                }
            } else {
                // 污点与方法返回值不同
                if (isMatch(taintValue, event.object)) {
                    // 污点与方法本身相同
                    boolean matchArg = false;
                    Object[] argTaint = new Object[event.argumentArray.length];
                    // 污点与方法返回值相同、本身不同、参数相同
                    // 如果参数存在，进入匹配环节，否则丢弃堆栈
                    int taintIndex = 0;
                    for (Object arg : event.argumentArray) {
                        // 只检测第一个匹配到的参数，存在漏掉的链路
                        if (isMatch(taintValue, arg)) {
                            argTaint[taintIndex] = arg;
                            matchArg = true;
                            taintIndex++;
                        }
                    }

                    // 污点与方法返回值不同、本身相同、参数不同
                    if (!matchArg) {
                        event.inValue = event.object;
                        matchStatus = true;
                    } else {
                        event.inValue = argTaint;
                        matchStatus = true;
                    }
                    if (matchStatus) {
                        event.outValue = event.object;
                    }
                } else {
                    // 污点与方法返回值、方法本身不相同
                    boolean matchArg = false;
                    Object[] argTaint = new Object[event.argumentArray.length];
                    // 污点与方法返回值相同、本身不同、参数相同
                    // 如果参数存在，进入匹配环节，否则丢弃堆栈
                    int taintIndex = 0;
                    for (Object arg : event.argumentArray) {
                        // 只检测第一个匹配到的参数，存在漏掉的链路
                        if (isMatch(taintValue, arg)) {
                            argTaint[taintIndex] = arg;
                            matchArg = true;
                            taintIndex++;
                        }
                    }

                    if (matchArg) {
                        event.inValue = argTaint;
                        event.outValue = argTaint;
                        matchStatus = true;
                    }
                }
            }
        } else {
            // 方法未执行完，说明是上层方法，加入污点传播链，但是不参与
            if (isMatch(taintValue, event.object)) {
                // 污点与方法本身相同
                boolean matchArg = false;
                Object[] argTaint = new Object[event.argumentArray.length];
                // 污点与方法本身不同、参数相同
                // 如果参数存在，进入匹配环节，否则丢弃堆栈
                int taintIndex = 0;
                for (Object arg : event.argumentArray) {
                    // 只检测第一个匹配到的参数，存在漏掉的链路
                    if (isMatch(taintValue, arg)) {
                        argTaint[taintIndex] = arg;
                        matchArg = true;
                        taintIndex++;
                    }
                }

                if (!matchArg) {
                    event.inValue = event.object;
                } else {
                    event.inValue = argTaint;
                }
            } else {
                // 污点与方法本身不相同
                boolean matchArg = false;
                Object[] argTaint = new Object[event.argumentArray.length];
                // 污点与方法返回值相同、本身不同、参数相同
                // 如果参数存在，进入匹配环节，否则丢弃堆栈
                int taintIndex = 0;
                for (Object arg : event.argumentArray) {
                    // 只检测第一个匹配到的参数，存在漏掉的链路
                    if (isMatch(taintValue, arg)) {
                        argTaint[taintIndex] = arg;
                        matchArg = true;
                        taintIndex++;
                    }
                }
                // 污点与方法返回值相同、本身不同、参数相同
                if (matchArg) {
                    event.inValue = argTaint;
                } else {
                    // 如果方法未执行完，且污点与参数、本身均不同，则加入当前污点为方法污点
                    event.inValue = taintValue;
                }
            }
            matchStatus = true;
        }
        return matchStatus;
    }

}
