package com.github.mhdirkse.codegen.plugin.impl;

import lombok.AccessLevel;
import lombok.Getter;

enum StatusCode {
    TEST_STATUS_ZERO_ARGS("Some status zero arguments"),
    TEST_STATUS_ONE_ARG("Some status about: {1}");

    StatusCode(final String formatString) {
        this.formatString = formatString;
    }

    @Getter(AccessLevel.PACKAGE)
    private final String formatString;

    String format(String... args) {
        String result = formatString;
        for(int i = 0; i < args.length; ++i) {
            String toReplace = String.format("\\Q{%d}\\E", i+1);
            result = result.replaceAll(
                    toReplace,
                    args[i]);
        }
        return result;
    }
}
