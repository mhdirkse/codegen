package com.github.mhdirkse.codegen.plugin.impl;

import lombok.Getter;

class ServiceFactory {
    @Getter
    private Runnable program;

    private FieldListerService fieldLister;
    FieldListerService fieldLister() {
        return fieldLister;
    }

    private StatusReportingService reporter;
    StatusReportingService reporter() {
        return reporter;
    }

    FieldService fieldService(final FieldServiceErrorCallback callback) {
        return new FieldService(this, callback);
    }

    ServiceFactory(final Runnable program, final Logger logger) {
        this(program, new StatusReportingServiceImpl(logger));
    }

    ServiceFactory(final Runnable program, final StatusReportingService reporter) {
        this.program = program;
        this.reporter = reporter;
        fieldLister = new FieldListerService(this);        
    }
}
