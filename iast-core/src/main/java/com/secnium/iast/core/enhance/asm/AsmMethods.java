package com.secnium.iast.core.enhance.asm;

import java.lang.iast.inject.Injecter;
import org.objectweb.asm.commons.Method;

/**
 * 常用的ASM method 集合 省得我到处声明
 *
 * @author luanjia@taobao.com
 * @date 16/5/21 Modified by dongzhiyong@huoxian.cn
 */
public interface AsmMethods {

    class InnerHelper {

        private InnerHelper() {
        }

        static Method getAsmMethod(final Class<?> clazz,
                final String methodName,
                final Class<?>... parameterClassArray) {
            return Method.getMethod(
                    SandboxReflectUtils.unCaughtGetClassDeclaredJavaMethod(clazz, methodName, parameterClassArray));
        }
    }

    /**
     * asm method of {@link #spyMethodOnBefore(Object, Object[], String, String, String, String, Object, String,
     * boolean, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnBefore = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodOnBefore",
            Object.class, Object[].class, String.class, String.class, String.class, String.class, String.class,
            Object.class, String.class, boolean.class, int.class
    );

    /**
     * asm method of {@link #spyMethodOnReturn(Object, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnReturn = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodOnReturn",
            Object.class,
            int.class
    );

    /**
     * asm method of {@link #spyMethodOnThrows(Throwable, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnThrows = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodOnThrows",
            Throwable.class,
            int.class
    );

    /**
     * asm method of {@link #spyMethodEnterPropagator()}
     */
    Method ASM_METHOD_Spy$spyMethodEnterPropagator = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodEnterPropagator"
    );

    /**
     * asm method of {@link #spyMethodLeavePropagator()}
     */
    Method ASM_METHOD_Spy$spyMethodLeavePropagator = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodLeavePropagator"
    );

    /**
     * asm method of {@link #isFirstLevelPropagator()}
     */
    Method ASM_METHOD_Spy$isFirstLevelPropagator = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelPropagator",
            String.class
    );

    /**
     * asm method of {@link #enterSink()}
     */
    Method ASM_METHOD_Spy$enterSink = InnerHelper.getAsmMethod(
            Injecter.class,
            "enterSink"
    );

    /**
     * asm method of {@link #leaveSink()}
     */
    Method ASM_METHOD_Spy$leaveSink = InnerHelper.getAsmMethod(
            Injecter.class,
            "leaveSink"
    );

    /**
     * asm method of {@link #isFirstLevelSink()}
     */
    Method ASM_METHOD_Spy$isFirstLevelSink = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelSink"
    );

    Method ASM_METHOD_Spy$hasTaint = InnerHelper.getAsmMethod(
            Injecter.class,
            "hasTaint"
    );

    /**
     * asm method of {@link #enterSource()}
     */
    Method ASM_METHOD_Spy$enterSource = InnerHelper.getAsmMethod(
            Injecter.class,
            "enterSource"
    );

    /**
     * asm method of {@link #leaveSource()}
     */
    Method ASM_METHOD_Spy$leaveSource = InnerHelper.getAsmMethod(
            Injecter.class,
            "leaveSource"
    );

    /**
     * asm method of {@link #isFirstLevelSource()}
     */
    Method ASM_METHOD_Spy$isFirstLevelSource = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelSource"
    );

    /**
     * asm method of {@link #enterHttp()}
     */
    Method ASM_METHOD_Spy$enterHttp = InnerHelper.getAsmMethod(
            Injecter.class,
            "enterHttp"
    );

    /**
     * asm method of {@link #leaveHttp(Object)}
     */
    Method ASM_METHOD_Spy$leaveHttp = InnerHelper.getAsmMethod(
            Injecter.class,
            "leaveHttp",
            Object.class
    );

    /**
     * asm method of {@link #isFirstLevelHttp()}
     */
    Method ASM_METHOD_Spy$isFirstLevelHttp = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelHttp"
    );

    /**
     * asm method of {@link #cloneRequest(Object, boolean)}
     */
    Method ASM_METHOD_Spy$cloneRequest = InnerHelper.getAsmMethod(
            Injecter.class,
            "cloneRequest",
            Object.class,
            boolean.class
    );

    Method ASM_METHOD_Spy$isReplayRequest = InnerHelper.getAsmMethod(
            Injecter.class,
            "isReplayRequest"
    );

    Method ASM_METHOD_Spy$cloneResponse = InnerHelper.getAsmMethod(
            Injecter.class,
            "cloneResponse",
            Object.class,
            boolean.class
    );


    /**
     * asm method of {@link #enterDubbo()}
     *
     * @since 1.2.0
     */
    Method ASM_METHOD_Spy$enterDubbo = InnerHelper.getAsmMethod(
            Injecter.class,
            "enterDubbo"
    );

    /**
     * asm method of {@link #leaveDubbo()}
     *
     * @since 1.2.0
     */
    Method ASM_METHOD_Spy$leaveDubbo = InnerHelper.getAsmMethod(
            Injecter.class,
            "leaveDubbo"
    );

    /**
     * asm method of {@link #isFirstLevelDubbo()}
     *
     * @since 1.2.0
     */
    Method ASM_METHOD_Spy$isFirstLevelDubbo = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelDubbo"
    );

}
