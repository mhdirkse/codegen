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

    ServiceFactory(final Runnable program, final Logger logger) {
        this.program = program;
        fieldLister = new FieldListerService(this);
        reporter = new StatusReportingServiceImpl(logger);
    }
}
