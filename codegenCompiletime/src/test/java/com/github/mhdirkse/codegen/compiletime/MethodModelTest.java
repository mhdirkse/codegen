package com.github.mhdirkse.codegen.compiletime;

import org.junit.Test;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

public class MethodModelTest extends MethodsUser {
    @Test
    public void testConstructionMethodReturningVoid() {
        MethodModel instance = new MethodModel(methodReturningVoid);
        Assert.assertEquals("testMethodReturningVoid", instance.getName());
        Assert.assertEquals("void", instance.getReturnType());
        Assert.assertThat(instance.getParameterTypes(), CoreMatchers.hasItems("int[]"));
    }

    @Test
    public void testConstructionMethodReturningIntArray() {
        MethodModel instance = new MethodModel(methodReturningIntArray);
        Assert.assertEquals("testMethodReturningIntArray", instance.getName());
        Assert.assertEquals("int[]", instance.getReturnType());
        Assert.assertThat(instance.getParameterTypes(), CoreMatchers.hasItems("java.lang.String"));
    }

    @Test
    public void testParameterTypesInitiallyNull() {
        MethodModel instance = new MethodModel();
        Assert.assertNull(instance.getParameterTypes());
        instance.setParameterTypes(null);
    }

    @Test
    public void testSetParameterTypesToNull() {
        MethodModel instance = new MethodModel(methodReturningVoid);
        instance.setParameterTypes(null);
        Assert.assertNull(instance.getParameterTypes());
    }

    @Test
    public void testGetFormalParameters() {
        MethodModel instance = new MethodModel(methodTwoParams);
        Assert.assertEquals("java.lang.String p1, int[] p2", instance.getFormalParametersInterface());
        Assert.assertEquals("final java.lang.String p1, final int[] p2", instance.getFormalParametersClass());
    }

    @Test
    public void testGetActualParameters() {
        MethodModel instance = new MethodModel(methodTwoParams);
        Assert.assertEquals("p1, p2", instance.getActualParameters());
    }

    @Test
    public void testGetActualParametersWith() {
        MethodModel instance = new MethodModel(methodTwoParams);
        Assert.assertEquals("p1, p2, ctx", instance.getActualParametersWith("ctx"));
    }

    @Test
    public void testAddParameterType() {
        MethodModel instance = new MethodModel(methodTwoParams);
        instance.addParameterType("int");
        Assert.assertEquals("java.lang.String p1, int[] p2, int p3", instance.getFormalParametersInterface());
    }

    @Test
    public void testWhenMethodModelCopiedThenParameterReferencesNotShared() {
        MethodModel original = new MethodModel(methodTwoParams);
        MethodModel copy = new MethodModel(original);
        Assert.assertThat(copy.getParameterTypes(), CoreMatchers.not(CoreMatchers.sameInstance(original.getParameterTypes())));
    }
}
