package com.github.mhdirkse.codegen.plugin.impl;

interface StatusReportingService {
    boolean hasErrors();
    void report(Status status);
}
