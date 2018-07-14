package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.runtime.HandlerStackContext;
import com.github.mhdirkse.codegen.test.output.MyInterfaceHandler;
import com.github.mhdirkse.codegen.test.output.MyInterfaceHandlerSelection;

public class MyInterfaceHandlerSelectionImpl implements MyInterfaceHandlerSelection {

    @Override
    public boolean firstMethod(int p1, HandlerStackContext<MyInterfaceHandler> p2) {
        return false;
    }
}
