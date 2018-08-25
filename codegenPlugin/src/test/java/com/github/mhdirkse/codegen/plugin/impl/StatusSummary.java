package com.github.mhdirkse.codegen.plugin.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class StatusSummary {
    StatusCode statusCode;
    String accessModifierOrTargetType;

    StatusSummary(final Status status) {
        this.statusCode = status.getStatusCode();
        String[] args = status.getArguments();
        if(args.length >= 3) {
            this.accessModifierOrTargetType = args[2];
        }
    }

    static Map<String, List<StatusSummary>> getSummary(final List<Status> statusses) {
        return statusses.stream()
                .collect(Collectors.groupingBy(
                        status -> status.getArguments()[1],
                        Collectors.mapping(StatusSummary::new, Collectors.toList())));
    }
}
