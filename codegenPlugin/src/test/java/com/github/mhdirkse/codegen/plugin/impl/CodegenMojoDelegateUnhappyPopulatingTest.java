package com.github.mhdirkse.codegen.plugin.impl;

import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_MISSING_ACCESS_MODIFIER;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_TYPE_MISMATCH;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_REQUIRED_CLASS_NOT_FOUND;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;
import com.github.mhdirkse.codegen.compiletime.TypeHierarchy;

@RunWith(Parameterized.class)
public class CodegenMojoDelegateUnhappyPopulatingTest extends CodegenMojoDelegateUnhappyTestBase {
    private static final String INPUT_CLASS_NAME = "com.github.mhdirkse.codegen.plugin.impl.CodegenMojoDelegateUnhappyPopulatingTest$TestInput";

    @SuppressWarnings("unused")
    private static class TestInput {
    }

    private static class TestProgram implements Runnable {
        @Input(INPUT_CLASS_NAME)
        ClassModel inputNotPublic;

        @Input(INPUT_CLASS_NAME)
        public static ClassModel inputIsStatic;

        @Input(INPUT_CLASS_NAME)
        public final ClassModel inputIsFinal = new ClassModel();
        
        @Input(INPUT_CLASS_NAME)
        public String inputNotClassModel;

        @Output("someTemplate")
        public String outputNotVelocityContext;

        @TypeHierarchy("xyz")
        public ClassModelList typeHierarchParentClassDoesNotExist;

        @TypeHierarchy(value = INPUT_CLASS_NAME, filterIsA = "xyz")
        public ClassModelList typeHierarchyFilterClassDoesNotExit;

        @Override
        public void run() {
        }
    }

    @Parameters(name = "Field {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"inputNotPublic", FIELD_MISSING_ACCESS_MODIFIER, "public"},
            {"inputIsStatic", FIELD_UNWANTED_ACCESS_MODIFIER, "static"},
            {"inputIsFinal", FIELD_UNWANTED_ACCESS_MODIFIER, "final"},
            {"inputNotClassModel", FIELD_TYPE_MISMATCH, "com.github.mhdirkse.codegen.compiletime.ClassModel"},
            {"outputNotVelocityContext", FIELD_TYPE_MISMATCH, "org.apache.velocity.VelocityContext"},
            {"typeHierarchParentClassDoesNotExist", FIELD_REQUIRED_CLASS_NOT_FOUND, "xyz"},
            {"typeHierarchyFilterClassDoesNotExit", FIELD_REQUIRED_CLASS_NOT_FOUND, "xyz"}
        });
    }

    @Parameter(2)
    public String accessModifierOrTargetType;

    @Before
    public void setUp() {
        super.setUp(new TestProgram());
    }

    @Override
    void runCodegenMojoDelegate(final CodegenMojoDelegate instance) {
        instance.populate();
    }

    @Test
    public void accessModifierOrTargetTypeMatches() {
        if(!StringUtils.isBlank(accessModifierOrTargetType)) {
            Assert.assertEquals(accessModifierOrTargetType,
                    actualStatusses.get(fieldName).get(0).accessModifierOrTargetType);
        }
    }
}
