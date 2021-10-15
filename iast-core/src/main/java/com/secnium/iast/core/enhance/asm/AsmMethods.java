package com.secnium.iast.core.enhance.asm;

import org.objectweb.asm.commons.Method;

import java.lang.iast.inject.Injecter;

/**
 * 常用的ASM method 集合
 * 省得我到处声明
 *
 * @author luanjia@taobao.com
 * @date 16/5/21
 * Modified by dongzhiyong@huoxian.cn
 */
public interface AsmMethods {

    class InnerHelper {
        private InnerHelper() {
        }

        static Method getAsmMethod(final Class<?> clazz,
                                   final String methodName,
                                   final Class<?>... parameterClassArray) {
            return Method.getMethod(SandboxReflectUtils.unCaughtGetClassDeclaredJavaMethod(clazz, methodName, parameterClassArray));
        }
    }

    /**
     * asm method of {@link Injecter#spyMethodOnBefore(Object, Object[], String, String, String, String, String, String, Object, String, boolean, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnBefore = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodOnBefore",
            Object.class, Object[].class, String.class, String.class, String.class, String.class, String.class, String.class, Object.class, String.class, boolean.class, int.class
    );

    /**
     * asm method of {@link Injecter#spyMethodOnReturn(Object, String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnReturn = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodOnReturn",
            Object.class, String.class, int.class
    );

    /**
     * asm method of {@link Injecter#spyMethodOnThrows(Throwable, String, int)}
     */
    Method ASM_METHOD_Spy$spyMethodOnThrows = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodOnThrows",
            Throwable.class, String.class, int.class
    );

    /**
     * asm method of {@link Injecter#spyMethodEnterPropagator(String)}
     */
    Method ASM_METHOD_Spy$spyMethodEnterPropagator = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodEnterPropagator",
            String.class
    );

    /**
     * asm method of {@link Injecter#spyMethodLeavePropagator(String)}
     */
    Method ASM_METHOD_Spy$spyMethodLeavePropagator = InnerHelper.getAsmMethod(
            Injecter.class,
            "spyMethodLeavePropagator",
            String.class
    );

    /**
     * asm method of {@link Injecter#isFirstLevelPropagator(String)}
     */
    Method ASM_METHOD_Spy$isFirstLevelPropagator = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelPropagator",
            String.class
    );

    /**
     * asm method of {@link Injecter#enterSink(String)}
     */
    Method ASM_METHOD_Spy$enterSink = InnerHelper.getAsmMethod(
            Injecter.class,
            "enterSink",
            String.class
    );

    /**
     * asm method of {@link Injecter#leaveSink(String)}
     */
    Method ASM_METHOD_Spy$leaveSink = InnerHelper.getAsmMethod(
            Injecter.class,
            "leaveSink",
            String.class
    );

    /**
     * asm method of {@link Injecter#isFirstLevelSink(String)}
     */
    Method ASM_METHOD_Spy$isFirstLevelSink = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelSink",
            String.class
    );

    Method ASM_METHOD_Spy$hasTaint = InnerHelper.getAsmMethod(
            Injecter.class,
            "hasTaint",
            String.class
    );

    /**
     * asm method of {@link Injecter#enterSource(String)}
     */
    Method ASM_METHOD_Spy$enterSource = InnerHelper.getAsmMethod(
            Injecter.class,
            "enterSource",
            String.class
    );

    /**
     * asm method of {@link Injecter#leaveSource(String)}
     */
    Method ASM_METHOD_Spy$leaveSource = InnerHelper.getAsmMethod(
            Injecter.class,
            "leaveSource",
            String.class
    );

    /**
     * asm method of {@link Injecter#isFirstLevelSource(String)}
     */
    Method ASM_METHOD_Spy$isFirstLevelSource = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelSource",
            String.class
    );

    /**
     * asm method of {@link Injecter#enterHttp(String)}
     */
    Method ASM_METHOD_Spy$enterHttp = InnerHelper.getAsmMethod(
            Injecter.class,
            "enterHttp",
            String.class
    );

    /**
     * asm method of {@link Injecter#leaveHttp(String, Object)}
     */
    Method ASM_METHOD_Spy$leaveHttp = InnerHelper.getAsmMethod(
            Injecter.class,
            "leaveHttp",
            String.class,
            Object.class
    );

    /**
     * asm method of {@link Injecter#isFirstLevelHttp(String)}
     */
    Method ASM_METHOD_Spy$isFirstLevelHttp = InnerHelper.getAsmMethod(
            Injecter.class,
            "isFirstLevelHttp",
            String.class
    );

    /**
     * asm method of {@link Injecter#cloneRequest(String, Object, boolean)}
     */
    Method ASM_METHOD_Spy$cloneRequest = InnerHelper.getAsmMethod(
            Injecter.class,
            "cloneRequest",
            String.class,
            Object.class,
            boolean.class
    );

    Method ASM_METHOD_Spy$isReplayRequest = InnerHelper.getAsmMethod(
            Injecter.class,
            "isReplayRequest",
            String.class
    );

    Method ASM_METHOD_Spy$cloneResponse = InnerHelper.getAsmMethod(
            Injecter.class,
            "cloneResponse",
            String.class,
            Object.class,
            boolean.class
    );

}
