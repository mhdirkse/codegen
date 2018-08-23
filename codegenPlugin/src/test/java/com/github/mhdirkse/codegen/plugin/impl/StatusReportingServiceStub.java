package com.github.mhdirkse.codegen.plugin.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

class StatusReportingServiceStub implements StatusReportingService {
    @Getter
    private final List<Status> statusses = new ArrayList<>();

    @Override
    public void report(final Status status) {
        statusses.add(status);
    }

    @Override
    public boolean hasErrors() {
        throw new IllegalStateException("Operation not supported");
    }

    @Override
    public void report(final Status status, final Throwable e) {
        statusses.add(status);
    }
}
