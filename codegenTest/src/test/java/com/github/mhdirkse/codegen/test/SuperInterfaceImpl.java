package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.runtime.HandlerStackContext;
import com.github.mhdirkse.codegen.test.output.MyInterfaceForSuperHandler;
import com.github.mhdirkse.codegen.test.output.SuperInterface;

public class SuperInterfaceImpl implements SuperInterface {
    @Override
    public boolean secondMethod(HandlerStackContext<MyInterfaceForSuperHandler> p1) {
        return false;
    }
}
