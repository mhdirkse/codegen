package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.runtime.HandlerStackContext;
import com.github.mhdirkse.codegen.test.output.MyInterfaceHandler;

public class MyHandlerImpl implements MyInterfaceHandler {
    @Override
    public boolean firstMethod(int p1, HandlerStackContext<MyInterfaceHandler> ctx) {
        return false;
    }

    @Override
    public boolean thirdMethod(String p1, int[] p2, final HandlerStackContext<MyInterfaceHandler> ctx) {
        return false;
    }

    @Override
    public boolean secondMethod(HandlerStackContext<MyInterfaceHandler> ctx) {
        return false;
    }
}
