package io.dongtai.iast.core.init.impl;

import io.dongtai.iast.core.bytecode.IastClassFileTransformer;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class TransformEngine implements IEngine {

    private Instrumentation inst;
    private IastClassFileTransformer classFileTransformer;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst, PolicyManager policyManager) {
        this.classFileTransformer = IastClassFileTransformer.getInstance(inst, policyManager);
        this.inst = inst;
    }

    @Override
    public void start() {
        try {
            DongTaiLog.debug("engine start to add transformer and retransform classes");
            inst.addTransformer(classFileTransformer, true);
            classFileTransformer.reTransform();
            DongTaiLog.debug("transform engine is successfully started");
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("TRANSFORM_ENGINE_START_FAILED"), e);
        }
    }

    @Override
    public void stop() {

    }

    /**
     * Clear bytecode modifications
     */

    @Override
    public void destroy() {
        inst.removeTransformer(classFileTransformer);
        HashMap<Object, byte[]> transformMap = IastClassFileTransformer.getTransformMap();
        int classSize = transformMap.size();
        ClassDefinition[] classDefinitions = new ClassDefinition[classSize];
        ArrayList<ClassDefinition> classDefinitionArrayList = new ArrayList<ClassDefinition>();
        Set<Object> classes = transformMap.keySet();
        for (Object aClass:classes){
            if (aClass instanceof String) {
                try {
                    Class<?> transformClass = Class.forName((String) aClass);
                    classDefinitionArrayList.add(new ClassDefinition(transformClass, transformMap.get(aClass)));
                } catch (ClassNotFoundException ignore) {
                }
            } else if (aClass instanceof Class<?>) {
                classDefinitionArrayList.add(new ClassDefinition((Class<?>) aClass, transformMap.get(aClass)));
            }
        }
        classDefinitionArrayList.toArray(classDefinitions);
        for (ClassDefinition classDefinition:classDefinitionArrayList){
            try {
                inst.redefineClasses(classDefinition);
            } catch (Throwable e) {
                DongTaiLog.error(ErrorCode.get("TRANSFORM_ENGINE_DESTROY_REDEFINE_CLASSES_FAILED"), e);
            }
        }
        inst = null;
        classFileTransformer = null;
    }
}
