package com.github.mhdirkse.codegen.plugin.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;

@RunWith(EasyMockRunner.class)
public class ClassServiceTest {
    private static final String PARENT_CLASS_NAME = Parent.class.getName();
    private StatusReportingServiceStub reporter;
    private ClassService instance;

    @Mock
    private ClassService.Callback callback;

    @Before
    public void setUp() {
        reporter = new StatusReportingServiceStub();
        ServiceFactory sf = new ServiceFactoryImpl(null, reporter, Parent.class.getClassLoader());
        instance = sf.classService();
    }

    @Test
    public void testWhenClassAvailableThenClassLoaded() {
        replay(callback);
        Optional<ClassService.ClassAdapter> actual = instance.loadClass(PARENT_CLASS_NAME, callback);
        verify(callback);
        Assert.assertEquals(Parent.class, actual.get().getAdaptee());
        Assert.assertEquals(0, reporter.getStatusses().size());
    }

    @Test
    public void testWhenClassNotAvailableThenCallbackCalled() {
        expect(callback.getStatusClassNotFound()).andReturn(
                Status.general(
                        StatusCode.TEST_CLASS_DOES_NOT_EXIST,
                        LogPriority.ERROR, "Parent"));
        replay(callback);
        Optional<ClassService.ClassAdapter> actual = instance.loadClass("doesNotExist", callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
        Assert.assertFalse(actual.isPresent());
    }

    @Test
    public void testGetHierarchyGivesChildrenAndParent() {
        ClassModelList actual = instance.getHierarchy(Parent.class, null);
        Assert.assertEquals(3, actual.size());
        Assert.assertThat(getSimpleNames(actual), CoreMatchers.hasItems(
                "Parent", "Child", "Child2"));
    }

    @Test
    public void testGetHierarchyCanFilterOnImplementedInterface() {
        ClassModelList actual = instance.getHierarchy(Parent.class, TestInterface.class);
        Assert.assertEquals(1, actual.size());
        Assert.assertThat(getSimpleNames(actual), CoreMatchers.hasItems("Child2"));
    }

    private List<String> getSimpleNames(List<ClassModel> classModels) {
        return classModels.stream()
                .map(ClassModel::getSimpleName)
                .map(s -> s.split("\\$")[1])
                .collect(Collectors.toList());
    }

    private class Parent {
    }

    @SuppressWarnings("unused")
    private class Child extends Parent {
    }

    interface TestInterface {
    }

    @SuppressWarnings("unused")
    private class Child2 extends Parent implements TestInterface {
    }
}
