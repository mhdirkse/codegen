package com.github.mhdirkse.codegen.plugin.impl;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;

abstract class ClassService {
    static interface Callback {
        Status getStatusClassNotFound();
    }

    private ServiceFactory sf;

    ClassService(final ServiceFactory sf) {
        this.sf = sf;
    }

    Optional<Class<?>> loadClass(final String fullName, final Callback callback) {
        try {
            return Optional.of(doLoadClass(fullName));
        }
        catch(Exception e) {
            Status status = callback.getStatusClassNotFound();
            sf.reporter().report(status, e);
            return Optional.empty();
        }
    }

    final <R> ClassModelList getHierarchy(final Class<R> root) {
        Reflections r = new Reflections(root.getPackage().getName());
        Set<Class<? extends R>> subClasses = r.getSubTypesOf(root);
        subClasses.add(root);
        ClassModelList result = new ClassModelList();
        result.addAll(subClasses.stream()
                .map(ClassModel::new)
                .collect(Collectors.toList()));
        return result;
    }

    abstract Class<?> doLoadClass(String fullName) throws ClassNotFoundException;
}
