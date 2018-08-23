package com.github.mhdirkse.codegen.plugin.impl;

import lombok.Getter;

abstract class ServiceFactory {
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

    abstract ClassService classService();

    private FieldService fieldService;
    FieldService fieldService() {
        return fieldService;
    }

    private VelocityContextService velocityContextService;
    VelocityContextService velocityContextService() {
        return velocityContextService;
    }

    ServiceFactory(final Runnable program, final Logger logger) {
        this(program, new StatusReportingServiceImpl(logger));
    }

    ServiceFactory(final Runnable program, final StatusReportingService reporter) {
        this.program = program;
        this.reporter = reporter;
        this.fieldLister = new FieldListerService(this);
        this.fieldService = new FieldService(this);
        this.velocityContextService = new VelocityContextService(this);
    }
}
