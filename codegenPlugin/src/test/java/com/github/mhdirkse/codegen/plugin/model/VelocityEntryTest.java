package com.github.mhdirkse.codegen.plugin.model;

import org.junit.Test;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

public class VelocityEntryTest {
    @Test
    public void testWhenClassModelNullThenCopyGivesNoError() {
        new VelocityEntry(new VelocityEntry());
    }

    @Test
    public void testWhenNoClassAndNoPropertyThenNull() {
        Assert.assertNull(new VelocityEntry().getObject());
    }

    @Test
    public void testWhenClassModelPresentThenReturnCopy() {
        VelocityEntry instance = new VelocityEntry();
        ClassModel originalClassModel = new ClassModel();
        instance.setClassModel(originalClassModel);
        ClassModel fromVelocityEntry = (ClassModel) instance.getObject();
        Assert.assertNotNull(fromVelocityEntry);
        Assert.assertThat(fromVelocityEntry, CoreMatchers.not(CoreMatchers.sameInstance(originalClassModel)));
    }

    @Test
    public void testWhenPropertyPresentThenReturnProperty() {
        VelocityEntry instance = new VelocityEntry();
        instance.setProperty("myValue");
        String valueFromVelocityEntry = (String) instance.getObject();
        Assert.assertNotNull(valueFromVelocityEntry);
        Assert.assertEquals("myValue", valueFromVelocityEntry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenBothClassAndPropertyPresentThenError() {
        VelocityEntry instance = new VelocityEntry();
        ClassModel originalClassModel = new ClassModel();
        instance.setClassModel(originalClassModel);
        instance.setProperty("myValue");
        instance.getObject();
    }
}
