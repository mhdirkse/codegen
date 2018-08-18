package com.github.mhdirkse.codegen.plugin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;

public class ClassLoaderAdapterTest {
    @Test
    public void testGetHierarchyGivesChildrenAndParent() {
        ClassLoaderAdapter instance = ClassLoaderAdapter.forCl(Parent.class.getClassLoader());
        ClassModelList actual = instance.getHierarchy(
                Parent.class);
        Assert.assertEquals(2, actual.size());
        Assert.assertThat(getSimpleNames(actual), CoreMatchers.hasItems(
                "Parent", "Child"));
    }

    private List<String> getSimpleNames(Collection<ClassModel> classModels) {
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
}
