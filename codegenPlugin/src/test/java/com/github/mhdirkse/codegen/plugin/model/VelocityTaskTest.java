package com.github.mhdirkse.codegen.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VelocityTaskTest {
    List<VelocityEntry> velocityEntries;

    @Before
    public void setUp() {
        ClassModel classModel = new ClassModel();
        classModel.setFullName("testpack.TestClass");
        classModel.setMethods(TestInput.class.getMethods());
        VelocityEntry velocityEntry = new VelocityEntry();
        velocityEntry.setEntryName("source");
        velocityEntry.setClassModel(classModel);
        velocityEntries = new ArrayList<>();
        velocityEntries.add(velocityEntry);
    }

    @Test
    public void testGetVelocityEntriesDoesDeepCopy() {
        VelocityTask instance = new VelocityTask();
        instance.setVelocityEntries(velocityEntries);
        Assert.assertFalse(instance.isVelocityEntriesSame(velocityEntries));
        Assert.assertEquals(velocityEntries.get(0).getEntryName(), instance.getVelocityEntries().get(0).getEntryName());
        Assert.assertThat(velocityEntries.get(0).getClassModel(), CoreMatchers.not(CoreMatchers.sameInstance(
                instance.getVelocityEntries().get(0).getClassModel())));
    }

    @Test
    public void testWhenVelocityEntriesNullThenNoException() {
        VelocityTask instance = new VelocityTask();
        instance.setVelocityEntries(null);
        Assert.assertNull(instance.getVelocityEntries());
    }
}
