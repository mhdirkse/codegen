package com.github.mhdirkse.codegen.plugin.model;

interface TestInput {
    void testMethodReturningVoid(final int[] parameter);
    int[] testMethodReturningIntArray(final String parameter);
    void testMethodTwoParams(final String p1, final int[] p2);
}
