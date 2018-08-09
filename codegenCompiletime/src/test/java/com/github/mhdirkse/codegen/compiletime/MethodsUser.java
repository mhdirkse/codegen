package com.github.mhdirkse.codegen.compiletime;

import java.lang.reflect.Method;

import org.junit.Before;

public class MethodsUser {
    Method methodReturningVoid = null;
    Method methodReturningIntArray = null;
    Method methodTwoParams = null;

    @Before
    public void setUp() throws NoSuchMethodException {
        Class<TestInput> clazz = TestInput.class;
        methodReturningVoid = clazz.getMethod("testMethodReturningVoid", int[].class);
        methodReturningIntArray = clazz.getMethod("testMethodReturningIntArray", String.class);
        methodTwoParams = clazz.getMethod("testMethodTwoParams", String.class, int[].class);
    }
}
