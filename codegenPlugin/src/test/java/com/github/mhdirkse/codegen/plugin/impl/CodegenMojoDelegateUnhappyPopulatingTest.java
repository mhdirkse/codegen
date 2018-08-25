package com.github.mhdirkse.codegen.plugin.impl;

import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_MISSING_ACCESS_MODIFIER;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Input;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

@RunWith(Parameterized.class)
public class CodegenMojoDelegateUnhappyPopulatingTest {
    private static final String INPUT_CLASS_NAME = "com.github.mhdirkse.codegen.plugin.impl.CodegenMojoDelegateUnhappyPopulatingTest$TestInput";

    private static class TestInput {
    }

    private static class TestProgram implements Runnable {
        @Input(INPUT_CLASS_NAME)
        ClassModel inputNotPublic;

        @Override
        public void run() {
        }
    }

    @Parameters(name = "Field {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"inputNotPublic", FIELD_MISSING_ACCESS_MODIFIER, "public"}
        });
    }

    @Parameter(0)
    public String fieldName;

    @Parameter(1)
    public StatusCode expectedStatusCode;

    @Parameter(2)
    public String accessModifierOrTargetType;

    private class StatusSummary {
        StatusCode statusCode;
        String fieldName;
        String accessModifierOrTargetType;

        StatusSummary(final Status status) {
            this.statusCode = status.getStatusCode();
            String[] args = status.getArguments();
            this.fieldName = args[1];
            if(args.length >= 3) {
                this.accessModifierOrTargetType = args[2];
            }
        }
    }

    Map<String, List<StatusSummary>> actualStatusses;

    @Before
    public void setUp() {
        StatusReportingServiceStub reporter = new StatusReportingServiceStub();
        TestProgram program = new TestProgram();
        ServiceFactory sf = new ServiceFactoryImpl(program, reporter, this.getClass().getClassLoader());
        CodegenMojoDelegate instance = new CodegenMojoDelegate(program, sf);
        instance.populate();
        actualStatusses = reporter.getStatusses().stream()
                .collect(Collectors.groupingBy(
                        status -> status.getArguments()[1],
                        Collectors.mapping(StatusSummary::new, Collectors.toList())));
    }

    @Test
    public void onlyExpectedErrorSeen() {
        Assert.assertEquals(1, actualStatusses.get(fieldName).size());
    }

    @Test
    public void statusCodeMatches() {
        Assert.assertEquals(expectedStatusCode, actualStatusses.get(fieldName).get(0).statusCode);
    }

    @Test
    public void accessModifierOrTargetTypeMatches() {
        if(!StringUtils.isBlank(accessModifierOrTargetType)) {
            Assert.assertEquals(accessModifierOrTargetType,
                    actualStatusses.get(fieldName).get(0).accessModifierOrTargetType);
        }
    }
}
