package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.runtime.HandlerStackContext;
import com.github.mhdirkse.codegen.test.output.MyInterfaceForSuperHandler;

public class MyInterfaceForSuperHandlerImpl implements MyInterfaceForSuperHandler {
    @Override
    public boolean secondMethod(HandlerStackContext<MyInterfaceForSuperHandler> p1) {
        return false;
    }

    @Override
    public boolean firstMethod(String p1, HandlerStackContext<MyInterfaceForSuperHandler> p2) {
        return false;
    }
}
