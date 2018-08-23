package com.github.mhdirkse.codegen.plugin.impl;

import lombok.AccessLevel;
import lombok.Getter;

enum StatusCode {
    TEST_STATUS_ZERO_ARGS("Some status zero arguments."),
    TEST_STATUS_ONE_ARG("Some status about: {1}."),
    TEST_STATUS_TWO_ARGS("Some status about: {1} and {2}."),

    FIELD_MISSING_ACCESS_MODIFIER("@{1} field {2} misses modifier {3}."),
    FIELD_UNWANTED_ACCESS_MODIFIER("@{1} field {2} should not be {3}."),
    FIELD_TYPE_MISMATCH("@{1} field {2} should be {3} but was {4}."),
    FIELD_GET_ERROR("@{1} field {2} could not be read."),
    FIELD_SET_ERROR("@{1} field {2} could not be set.");
    
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
