package com.github.mhdirkse.codegen.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.mhdirkse.codegen.test.output.MyInterfaceAbstractHandler;
import com.github.mhdirkse.codegen.test.output.MyInterfaceHandler;

public class MyInterfaceAbstractHandlerClassTest {
    @Test
    public void testWhenMyInterfaceAbstractHandlerConstructedFalseThenReturnFalse() {
        MyInterfaceHandler instance = new MyInterfaceAbstractHandler(false);
        Assert.assertFalse(instance.firstMethod(0, null));
        Assert.assertFalse(instance.secondMethod(null));
        Assert.assertFalse(instance.thirdMethod("aap", new int[] {0, 0}, null));
    }

    @Test
    public void testWhenMyInterfaceAbstractHandlerConstructedTrueThenReturnTrue() {
        MyInterfaceHandler instance = new MyInterfaceAbstractHandler(true);
        Assert.assertTrue(instance.firstMethod(0, null));
        Assert.assertTrue(instance.secondMethod(null));
        Assert.assertTrue(instance.thirdMethod("aap", new int[] {0, 0}, null));
    }
}
