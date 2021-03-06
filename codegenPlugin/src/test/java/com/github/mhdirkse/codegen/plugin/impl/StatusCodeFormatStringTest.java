package com.github.mhdirkse.codegen.plugin.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.github.mhdirkse.utils.AbstractStatusCode;

@RunWith(Parameterized.class)
public class StatusCodeFormatStringTest {
    @Parameters(name = "Enum value {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(StatusCode.values()).stream()
                .map(sc -> new Object[] {sc})
                .collect(Collectors.toList());
    }

    @Parameter
    public StatusCode statusCode;

    @Test
    public void allFormattersAreBracketDigitsBracket() {
        String validFormattersRemoved = statusCode.getFormatString()
                .replaceAll(AbstractStatusCode.anyFormatter(), "");
        Assert.assertFalse(validFormattersRemoved.contains("{"));
        Assert.assertFalse(validFormattersRemoved.contains("}"));
    }

    @Test
    public void allFormattersEndWithDot() {
        String fmt = statusCode.getFormatString();
        Assert.assertEquals(
                Character.valueOf('.').toString(),
                Character.valueOf(fmt.charAt(fmt.length() - 1)).toString());
    }
}
