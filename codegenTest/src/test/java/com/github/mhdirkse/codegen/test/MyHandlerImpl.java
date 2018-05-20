package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.test.output.MyInterfaceHandler;

public class MyHandlerImpl implements MyInterfaceHandler {
    @Override
    public boolean firstMethod(int p1) {
        return false;
    }

    @Override
    public boolean thirdMethod(String p1, int[] p2) {
        return false;
    }

    @Override
    public boolean secondMethod() {
        return false;
    }
}
