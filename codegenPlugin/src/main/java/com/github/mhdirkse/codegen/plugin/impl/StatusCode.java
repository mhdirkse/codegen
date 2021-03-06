package com.github.mhdirkse.codegen.plugin.impl;

import com.github.mhdirkse.utils.AbstractStatusCode;

enum StatusCode implements AbstractStatusCode {
    TEST_STATUS_ZERO_ARGS("Some status zero arguments."),
    TEST_STATUS_ONE_ARG("Some status about: {1}."),
    TEST_STATUS_TWO_ARGS("Some status about: {1} and {2}."),
    TEST_CLASS_DOES_NOT_EXIST("Class {1} does not exist."),

    FIELD_MISSING_ACCESS_MODIFIER("@{1} field {2} misses modifier {3}."),
    FIELD_UNWANTED_ACCESS_MODIFIER("@{1} field {2} should not be {3}."),
    FIELD_TYPE_MISMATCH("@{1} field {2} should be {3} but was {4}."),
    FIELD_GET_ERROR("@{1} field {2} could not be read."),
    FIELD_SET_ERROR("@{1} field {2} could not be set."),
    FIELD_GET_ERROR_AFTER_PROGRAM_RUN(
            "@{1} field {2} was not available after running your Codegen program. It may have cleared the field."),
    FIELD_REQUIRED_CLASS_NOT_FOUND("@{1} field {2} requires class {3}, but it is not in your dependencies."),
    FIELD_VELOCITY_CONTEXT_LACKS_TARGET("@{1} field {2}: VelocityContext does not have key \"target\"."),
    FIELD_VELOCITY_CONTEXT_TARGET_NOT_CLASS_MODEL(
            "@{1} field {2}: The \"target\" should be a {3}, but was {4}."),
    FIELD_VELOCITY_CONTEXT_CLASS_MODEL_NO_FULL_NAME(
            "@{1} field {2}: The ClassModel found with key \"target\" lacks a class name (fullName)."),
    FILE_WRITE_IO_ERROR("IO error."),

    UNKNOWN_FIELD_ERROR("@{1} field {2}: unknown error."),
    UNKONWN_FIELD_ERROR_AFTER_PROGRAM_RUN(
            "@{1} field {2} was not accessible after your Codegen program ran.");

    private String formatString;
    @Override
    public String getFormatString() {
        return formatString;
    }

    StatusCode(final String formatString) {
        this.formatString = formatString;
    }
}
