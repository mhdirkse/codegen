package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;

import static org.easymock.EasyMock.*;

@RunWith(EasyMockRunner.class)
public class FieldListerServiceTest {
    private FieldListerService service;
    private StatusReportingServiceStub statusReportingService;

    @Mock
    private FieldListerService.Callback callback;

    private class Program implements Runnable {
        @Input("some.package.SomeClass")
        public ClassModel inputClass;

        @Output("someTemplate")
        VelocityContext velocityContext;

        @Override
        public void run() {
        }
    }

    @Before
    public void setUp() {
        statusReportingService = new StatusReportingServiceStub();
        service = new ServiceFactoryImpl(new Program(), statusReportingService, null).fieldLister();
    }

    @Test
    public void testGetFieldsWithAnnotationInput() {
        replay(callback);
        Set<Field> inputFields = service.getFields(Input.class, callback);
        verify(callback);
        Assert.assertEquals(1, inputFields.size());
        inputFields.forEach(f -> 
            Assert.assertEquals("inputClass", f.getName()));
    }

    @Test
    public void whenFieldIsNotPublicThenError() throws NoSuchFieldException {
        Field field = Program.class.getDeclaredField("velocityContext");
        expect(callback.getStatusAccessModifierError(field, "public")).andReturn(
                Status.forFieldError(
                        StatusCode.FIELD_MISSING_ACCESS_MODIFIER,
                        Output.class, field, "public"));
        replay(callback);
        Set<Field> result = service.getFields(Output.class, callback);
        verify(callback);
        Assert.assertEquals(0, result.size());
        Assert.assertEquals(1, statusReportingService.getStatusses().size());
    }

    @Test
    public void testGetFieldsCanReturnEmptySet() {
        replay(callback);
        Set<Field> shouldBeEmpty = service.getFields(Override.class, callback);
        verify(callback);
        Assert.assertEquals(0, shouldBeEmpty.size());
    }
}
