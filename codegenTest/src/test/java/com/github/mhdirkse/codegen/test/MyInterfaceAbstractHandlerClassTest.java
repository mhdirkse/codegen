package com.github.mhdirkse.codegen.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.mhdirkse.codegen.test.output.MyInterfaceAbstractHandler;
import com.github.mhdirkse.codegen.test.output.MyInterfaceHandler;

public class MyInterfaceAbstractHandlerClassTest {
    @Test
    public void testWhenMyInterfaceAbstractHandlerConstructedFalseThenReturnFalse() {
        MyInterfaceHandler instance = new MyInterfaceAbstractHandler(false);
        Assert.assertFalse(instance.firstMethod(0));
        Assert.assertFalse(instance.secondMethod());
        Assert.assertFalse(instance.thirdMethod("aap", new int[] {0, 0}));
    }

    @Test
    public void testWhenMyInterfaceAbstractHandlerConstructedTrueThenReturnTrue() {
        MyInterfaceHandler instance = new MyInterfaceAbstractHandler(true);
        Assert.assertTrue(instance.firstMethod(0));
        Assert.assertTrue(instance.secondMethod());
        Assert.assertTrue(instance.thirdMethod("aap", new int[] {0, 0}));
    }
}
