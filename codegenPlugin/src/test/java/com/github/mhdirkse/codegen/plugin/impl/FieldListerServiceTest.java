package com.github.mhdirkse.codegen.plugin.impl;

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;

public class FieldListerServiceTest {
    private FieldListerService service;

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
        service = new ServiceFactory(new Program(), (Logger) null).fieldLister();
    }

    @Test
    public void testGetFieldsWithAnnotationInput() {
        Set<Field> inputFields = service.getFields(Input.class);
        Assert.assertEquals(1, inputFields.size());
        inputFields.forEach(f -> 
            Assert.assertEquals("inputClass", f.getName()));
    }

    @Test
    public void testGetFieldsWithAnnotationOutput() {
        Set<Field> outputFields = service.getFields(Output.class);
        Assert.assertEquals(1, outputFields.size());
        outputFields.forEach(f -> 
            Assert.assertEquals("velocityContext", f.getName()));
    }

    @Test
    public void testGetFieldsCanReturnEmptySet() {
        Set<Field> shouldBeEmpty = service.getFields(Override.class);
        Assert.assertEquals(0, shouldBeEmpty.size());
    }
}
