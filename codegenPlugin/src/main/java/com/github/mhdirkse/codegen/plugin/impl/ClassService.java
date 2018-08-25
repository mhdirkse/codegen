package com.github.mhdirkse.codegen.plugin.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;

import lombok.Getter;
import lombok.Setter;

public abstract class ClassService {
    static interface Callback {
        Status getStatusClassNotFound();
    }

    static class ClassAdapter {
        @Getter
        @Setter
        Class<?> adaptee;

        String getName() {
            return adaptee.getName();
        }
    }

    private ServiceFactory sf;

    protected ClassService(final ServiceFactory sf) {
        this.sf = sf;
    }

    Optional<ClassService.ClassAdapter> loadClass(final String fullName, final Callback callback) {
        try {
            ClassService.ClassAdapter result = new ClassService.ClassAdapter();
            result.setAdaptee(doLoadClass(fullName));
            return Optional.of(result);
        }
        catch(Exception e) {
            Status status = callback.getStatusClassNotFound();
            sf.reporter().report(status, e);
            return Optional.empty();
        }
    }

    final <R> ClassModelList getHierarchy(final Class<R> root, final Class<?> optionalFilterClass) {
        Reflections r = new Reflections(root.getPackage().getName());
        Set<Class<? extends R>> subClasses = r.getSubTypesOf(root);
        subClasses.add(root);
        subClasses = filterInterface(optionalFilterClass, subClasses);
        ClassModelList result = new ClassModelList();
        result.addAll(subClasses.stream()
                .map(ClassModel::new)
                .collect(Collectors.toList()));
        return result;
    }

    private <R> Set<Class<? extends R>> filterInterface(final Class<?> interfaceToInherit,
            Set<Class<? extends R>> subClasses) {
        if (Objects.nonNull(interfaceToInherit)) {
            subClasses = subClasses.stream()
                    .filter((s) -> interfaceToInherit.isAssignableFrom(s)).collect(Collectors.toSet());
        }
        return subClasses;
    }

    protected abstract Class<?> doLoadClass(String fullName) throws ClassNotFoundException;
}
