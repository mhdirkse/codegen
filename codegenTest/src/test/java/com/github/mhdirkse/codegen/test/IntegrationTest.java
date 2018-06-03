package com.github.mhdirkse.codegen.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.runtime.HandlerStackContext;
import com.github.mhdirkse.codegen.test.output.MyInterfaceAbstractHandler;
import com.github.mhdirkse.codegen.test.output.MyInterfaceDelegator;
import com.github.mhdirkse.codegen.test.output.MyInterfaceHandler;

public class IntegrationTest implements OutputSink {
    static String combine(final String keyword, final int passedArgument) {
        return String.format("%s: %d", keyword, passedArgument);
    }

    private static abstract class MyInterfaceHandlerStubBase extends MyInterfaceAbstractHandler {
        final OutputSink outputSink;
        MyInterfaceHandlerStubBase(final OutputSink outputSink) {
            super(true);
            this.outputSink = outputSink;
        }
 
        @Override
        public boolean firstMethod(final int x, final HandlerStackContext<MyInterfaceHandler> ctx) {
            outputSink.output(IntegrationTest.combine(this.getClass().getSimpleName(), x));
            afterFirstMethod(ctx);
            return true;
        }

        abstract void afterFirstMethod(final HandlerStackContext<MyInterfaceHandler> ctx);
    }

    private static class MyInterfaceHandlerStubFirst extends MyInterfaceHandlerStubBase {
        MyInterfaceHandlerStubFirst(final OutputSink outputSink) {
            super(outputSink);
        }

        @Override
        void afterFirstMethod(final HandlerStackContext<MyInterfaceHandler> ctx) {
            ctx.addFirst(new MyInterfaceHandlerStubSecond(outputSink));
        }
    }

    private static class MyInterfaceHandlerStubSecond extends MyInterfaceHandlerStubBase {
        MyInterfaceHandlerStubSecond(final OutputSink outputSink) {
            super(outputSink);
        }

        @Override
        void afterFirstMethod(final HandlerStackContext<MyInterfaceHandler> ctx) {
        }
    }

    private List<String> outputs;

    private MyInterfaceDelegator instance;

    @Override
    public void output(final String s) {
        outputs.add(s);
    }

    @Before
    public void setUp() {
        outputs = new ArrayList<>();
        instance = new MyInterfaceDelegator(new MyInterfaceHandlerStubFirst(this));
    }

    @Test
    public void testDelegatorAllowsHandlersToChangeStack() {
        instance.firstMethod(1);
        Assert.assertEquals(1, outputs.size());
        Assert.assertEquals(combine(MyInterfaceHandlerStubFirst.class.getSimpleName(), 1), outputs.get(0));
        instance.firstMethod(2);
        Assert.assertEquals(2, outputs.size());
        Assert.assertEquals(combine(MyInterfaceHandlerStubFirst.class.getSimpleName(), 1), outputs.get(0));
        Assert.assertEquals(combine(MyInterfaceHandlerStubSecond.class.getSimpleName(), 2), outputs.get(1));
    }
}
