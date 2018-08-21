package com.github.mhdirkse.codegen.plugin.impl;

import org.junit.Assert;
import org.junit.Test;

import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.TEST_STATUS_ZERO_ARGS;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.TEST_STATUS_ONE_ARG;

public class StatusCodeTest {
    @Test
    public void whenStatusFormatsNoArgumentsAndNoArgumentsGivenThenNoFormattingDone() {
        Assert.assertEquals("Some status zero arguments", TEST_STATUS_ZERO_ARGS.format());
    }

    @Test
    public void whenStatusFormatsOneArgumentAndOneArgumentGivenThenFormatApplied() {
        Assert.assertEquals("Some status about: arg1", TEST_STATUS_ONE_ARG.format("arg1"));
    }
}
