package io.dongtai.iast.core.bytecode.enhance;

import io.dongtai.iast.core.bytecode.IastClassFileTransformer;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author CC11001100
 */
public class IastClassDiagramTest {

    @Test
    public void getInstance() {
        IastClassDiagram instance = IastClassDiagram.getInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void getClassAncestorSet() {

    }

    @Test
    public void setClassAncestorSet() {
    }

    @Test
    public void updateAncestorsByClassContext() throws IOException {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(IastClassFileTransformer.class.getName().replace(".", "/") + ".class");
        Assert.assertNotNull(resourceAsStream);
        ClassReader classReader = new ClassReader(resourceAsStream);
        ClassContext classContext = new ClassContext(classReader, IastClassFileTransformer.class.getClassLoader());
        Set<String> strings = IastClassDiagram.getInstance().updateAncestorsByClassContext(IastClassFileTransformer.class.getClassLoader(), classContext);
        Assert.assertEquals("[io.dongtai.iast.core.bytecode.IastClassFileTransformer, java.lang.instrument.ClassFileTransformer]", strings.toString());
    }

    @Test
    public void getFamilyFromClass() {
    }

}