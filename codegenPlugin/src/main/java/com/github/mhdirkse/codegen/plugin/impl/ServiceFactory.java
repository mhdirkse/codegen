package com.github.mhdirkse.codegen.plugin.impl;

import lombok.Getter;

class ServiceFactory {
    @Getter
    private Runnable program;

    private FieldListerService fieldLister;

    FieldListerService fieldLister() {
        return fieldLister;
    }

    ServiceFactory(final Runnable program) {
        this.program = program;
        fieldLister = new FieldListerService(this);
    }
}
