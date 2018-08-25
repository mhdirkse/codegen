package com.github.mhdirkse.codegen.plugin.impl;

import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Output;

@RunWith(Parameterized.class)
public class CodegenMojoDelegateUnhappyWritingFilesTest extends CodegenMojoDelegateUnhappyTestBase {
    @SuppressWarnings("unused")
    private static class TestInput {
    }

    private static class TestProgram implements Runnable {
        @Output("dummyTemplate")
        public VelocityContext empty;

        @Output("dummyTemplate")
        public VelocityContext lacksTarget;

        @Output("dummyTemplate")
        public VelocityContext targetIsNotClassModel;

        @Output("dummyTemplate")
        public VelocityContext classModelNoFullName;

        @Override
        public void run() {
            // Undo populating to test that doing so is detected.
            empty = null;
            lacksTarget = new VelocityContext();
            targetIsNotClassModel = new VelocityContext();
            targetIsNotClassModel.put("target", "some string");
            classModelNoFullName = new VelocityContext();
            ClassModel cm = new ClassModel();
            classModelNoFullName.put("target", cm);
        }
    }

    @Parameters(name = "Field {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"empty", FIELD_GET_ERROR_AFTER_PROGRAM_RUN},
            {"lacksTarget", FIELD_VELOCITY_CONTEXT_LACKS_TARGET},
            {"targetIsNotClassModel", FIELD_VELOCITY_CONTEXT_TARGET_NOT_CLASS_MODEL},
            {"classModelNoFullName", FIELD_VELOCITY_CONTEXT_CLASS_MODEL_NO_FULL_NAME}
        });
    }

    @Before
    public void setUp() {
        super.setUp(new TestProgram());
    }

    @Override
    void runCodegenMojoDelegate(final CodegenMojoDelegate instance) {
        instance.run();
    }
}
