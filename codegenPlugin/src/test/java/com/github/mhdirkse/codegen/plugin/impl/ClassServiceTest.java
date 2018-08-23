package com.github.mhdirkse.codegen.plugin.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collection;
import java.util.List;
import java.util.Set;
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
        Class<?> actual = instance.loadClass(PARENT_CLASS_NAME, callback).get();
        verify(callback);
        Assert.assertEquals(Parent.class, actual);
        Assert.assertEquals(0, reporter.getStatusses().size());
    }

    @Test
    public void testWhenClassNotAvailableThenCallbackCalled() {
        expect(callback.getStatusClassNotFound()).andReturn(
                Status.general(
                        StatusCode.TEST_CLASS_DOES_NOT_EXIST,
                        LogPriority.ERROR, "Parent"));
        replay(callback);
        instance.loadClass("doesNotExist", callback);
        verify(callback);
        Assert.assertEquals(1, reporter.getStatusses().size());
    }

    @Test
    public void testGetHierarchyGivesChildrenAndParent() {
        Set<Class<? extends Parent>> actual = instance.getHierarchy(Parent.class);
        Assert.assertEquals(3, actual.size());
        Assert.assertThat(getSimpleNames(actual), CoreMatchers.hasItems(
                "Parent", "Child", "Child2"));
    }

    private List<String> getSimpleNames(Collection<Class<? extends Parent>> classes) {
        ClassModelList classModels = new ClassModelList(
                classes.stream().map(ClassModel::new).collect(Collectors.toList()));
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
