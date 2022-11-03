package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Modifier;

public class SignatureMethodMatcherTest {
    @Test
    public void testMatch() {
        Signature signature;
        MethodMatcher methodMatcher;
        ClassContext classContext;
        MethodContext methodContext;

        signature = new Signature("Foo", "bar", new String[]{});
        methodMatcher = new SignatureMethodMatcher(signature);
        classContext = new ClassContext("Foo", null, null, Modifier.PUBLIC, false);
        classContext.setMatchedClassName("Foo");
        methodContext = new MethodContext(classContext, "bar");
        methodContext.setParameters(new String[]{});
        Assert.assertTrue("match " + signature, methodMatcher.match(methodContext));

        signature = new Signature("Foo", "bar", new String[]{"int"});
        methodMatcher = new SignatureMethodMatcher(signature);
        classContext = new ClassContext("Foo", null, null, Modifier.PUBLIC, false);
        classContext.setMatchedClassName("Foo");
        methodContext = new MethodContext(classContext, "bar");
        methodContext.setParameters(new String[]{"int"});
        Assert.assertTrue("match " + signature, methodMatcher.match(methodContext));

        signature = new Signature("Foo1", "bar", new String[]{"int"});
        methodMatcher = new SignatureMethodMatcher(signature);
        classContext = new ClassContext("Foo", null, null, Modifier.PUBLIC, false);
        classContext.setMatchedClassName("Foo");
        methodContext = new MethodContext(classContext, "bar");
        methodContext.setParameters(new String[]{"int"});
        Assert.assertFalse("match " + signature, methodMatcher.match(methodContext));

        signature = new Signature("Foo", "bar1", new String[]{"int"});
        methodMatcher = new SignatureMethodMatcher(signature);
        classContext = new ClassContext("Foo", null, null, Modifier.PUBLIC, false);
        classContext.setMatchedClassName("Foo");
        methodContext = new MethodContext(classContext, "bar");
        methodContext.setParameters(new String[]{"int"});
        Assert.assertFalse("match " + signature, methodMatcher.match(methodContext));

        signature = new Signature("Foo", "bar", new String[]{});
        methodMatcher = new SignatureMethodMatcher(signature);
        classContext = new ClassContext("Foo", null, null, Modifier.PUBLIC, false);
        classContext.setMatchedClassName("Foo");
        methodContext = new MethodContext(classContext, "bar");
        methodContext.setParameters(new String[]{"int"});
        Assert.assertFalse("match " + signature, methodMatcher.match(methodContext));
    }
}