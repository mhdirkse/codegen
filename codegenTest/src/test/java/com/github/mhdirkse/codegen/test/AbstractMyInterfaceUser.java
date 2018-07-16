package com.github.mhdirkse.codegen.test;

import com.github.mhdirkse.codegen.test.input.MyInterface;
import com.github.mhdirkse.codegen.test.output.AbstractMyInterface;

public class AbstractMyInterfaceUser implements MyInterface {
    private final AbstractMyInterface instance;

    public AbstractMyInterfaceUser(final AbstractMyInterface instance) {
        this.instance = instance;
    }

    @Override
    public void firstMethod(int x) {
        instance.firstMethod(x);
    }

    @Override
    public void secondMethod() {
        instance.secondMethod();
    }

    @Override
    public void thirdMethod(String x, int[] y) {
        instance.thirdMethod(x, y);
    }
}
