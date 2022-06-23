package io.dongtai.iast.core.init.impl;

import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.init.IEngine;
import io.dongtai.iast.core.bytecode.IastClassFileTransformer;
import io.dongtai.log.DongTaiLog;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class TransformEngine implements IEngine {

    private Instrumentation inst;
    private IastClassFileTransformer classFileTransformer;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        this.classFileTransformer = IastClassFileTransformer.getInstance(inst);
        this.inst = inst;
    }

    @Override
    public void start() {
        try {
            DongTaiLog.debug("Install data acquisition and analysis sub-modules");
            inst.addTransformer(classFileTransformer, true);
            classFileTransformer.reTransform();
            DongTaiLog.debug("The sub-module of data acquisition and analysis is successfully installed");
        } catch (Throwable cause) {
            DongTaiLog.error("Failed to install the sub-module of data collection and analysis");
            DongTaiLog.error(cause);
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
        ArrayList<ClassDefinition> classDefinitionArrayList = new ArrayList<>();
        Set<Object> classes = transformMap.keySet();
        for (Object aClass:classes){
            if(aClass instanceof String){
                try {
                    Class<?> transformClass = Class.forName((String) aClass);
                    classDefinitionArrayList.add(new ClassDefinition(transformClass,transformMap.get(aClass)));
                } catch (ClassNotFoundException e) {
                    DongTaiLog.debug(e);
                }
            }else if (aClass instanceof Class<?>){
                classDefinitionArrayList.add(new ClassDefinition((Class<?>) aClass,transformMap.get(aClass)));
            }
        }
        classDefinitionArrayList.toArray(classDefinitions);
        try {
            inst.redefineClasses(classDefinitions);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            DongTaiLog.error(e);
        }
        inst = null;
        classFileTransformer = null;
    }
}
