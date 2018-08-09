package com.github.mhdirkse.codegen.compiletime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClassModelTest extends MethodsUser {
    ClassModel instance;
    Map<String, MethodModel> instanceMethods;
    
    @Before
    public void setUp() throws NoSuchMethodException {
        super.setUp();
        instance = new ClassModel();
        instance.setFullName("testpack.TestClass");
        instance.setMethods(TestInput.class.getMethods());
        instanceMethods = getMethodsByNameMap(instance);
    }

    @Test
    public void testClassModelConstruction() {
        Assert.assertEquals("testpack.TestClass", instance.getFullName());
        Assert.assertEquals("TestClass", instance.getSimpleName());
        Assert.assertEquals("testpack", instance.getPackage());
        Assert.assertThat(instanceMethods.keySet(), CoreMatchers.hasItems(
                "testMethodReturningVoid", "testMethodReturningIntArray", "testMethodTwoParams"));
    }

    private Map<String, MethodModel> getMethodsByNameMap(final ClassModel cm) {
        HashMap<String, MethodModel> result = new HashMap<>();
        for (MethodModel mm : cm.getMethods()) {
            result.put(mm.getName(), mm);
        }
        return result;
    }

    @Test
    public void testCopyConstructorClassModelDoesDeepCopy() {
        ClassModel copy = new ClassModel(instance);
        Assert.assertEquals("testpack.TestClass", copy.getFullName());
        Map<String, MethodModel> copyMethods = getMethodsByNameMap(copy);
        Assert.assertThat(copyMethods.keySet(), CoreMatchers.hasItems(
                "testMethodReturningVoid", "testMethodReturningIntArray", "testMethodTwoParams"));
        MethodModel aInstanceMethod = instanceMethods.get("testMethodReturningVoid");
        MethodModel aCopyMethod = copyMethods.get("testMethodReturningVoid");
        Assert.assertThat(aCopyMethod, CoreMatchers.not(CoreMatchers.sameInstance(aInstanceMethod)));
        Assert.assertFalse(aCopyMethod.sharesParameterTypesRef(aInstanceMethod));
    }

    @Test
    public void testWhenMethodsNullThenCopyWithoutException() {
        ClassModel instance = new ClassModel();
        Assert.assertNull(instance.getMethods());
        Assert.assertNull(new ClassModel(instance).getMethods());
    }

    @Test
    public void testSetReturnTypeForAllMethods() {
        instance.setReturnTypeForAllMethods("boolean");
        Assert.assertEquals("boolean", instance.getMethods().get(0).getReturnType());
    }

    @Test
    public void testAddParameterTypeToAllMethods() {
        instance.addParameterTypeToAllMethods("boolean");
        Map<String, MethodModel> methods = getMethodsByNameMap(instance);
        Assert.assertEquals("int[] p1, boolean p2", methods.get("testMethodReturningVoid").getFormalParametersInterface());
    }

    @Test
    public void testGetMethodReturnTypes() {
        Assert.assertThat(instance.getReturnTypes(), CoreMatchers.hasItems("int[]", "void"));
    }

    @Test
    public void testCanFormatNullIntoString() {
        Assert.assertEquals("null", String.format("%s", (String) null));
    }

    @Test
    public void testSelectMethods() {
        List<MethodModel> selectedMethods = instance.selectMethods(
                new HashSet<String>(Arrays.asList("testMethodReturningVoid")));
        Assert.assertEquals(1, selectedMethods.size());
        Assert.assertEquals("testMethodReturningVoid", selectedMethods.get(0).getName());
    }

    @Test
    public void tesSetOverridden() {
        instance.setOverridden(new HashSet<String>(Arrays.asList("testMethodReturningIntArray", "testMethodTwoParams")));
        instanceMethods = getMethodsByNameMap(instance);
        Assert.assertFalse(instanceMethods.get("testMethodReturningVoid").getOverridden());
        Assert.assertTrue(instanceMethods.get("testMethodReturningIntArray").getOverridden());
        Assert.assertTrue(instanceMethods.get("testMethodTwoParams").getOverridden());
    }
}
