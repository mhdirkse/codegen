package com.github.mhdirkse.codegen.plugin.impl;

class StatusReportingServiceImpl implements StatusReportingService {
    private final Logger logger;
    private boolean hasErrors = false;

    StatusReportingServiceImpl(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean hasErrors() {
        return hasErrors;
    }

    @Override
    public void report(final Status status) {
        String msg = update(status);
        status.getLogPriority().log(msg, logger);
    }

    @Override
    public void report(final Status status, final Throwable e) {
        String msg = update(status);
        status.getLogPriority().log(msg, e, logger);
    }

    private String update(final Status status) {
        if(status.getLogPriority() == LogPriority.ERROR) {hasErrors = true;};
        String msg = status.getStatusCode().format(status.getArguments());
        return msg;
    }
}
