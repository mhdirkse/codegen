package com.github.mhdirkse.codegen.plugin;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.reflections.Reflections;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;

abstract class ClassLoaderAdapter {
    abstract Class<?> loadClass(final String name) throws ClassNotFoundException;
    abstract ClassLoader getClassLoader();

    final <R> ClassModelList getHierarchy(final Class<R> root, final Class<?> interfaceToInherit) {
        Reflections r = new Reflections(root.getPackage().getName());
        Set<Class<? extends R>> subClasses = r.getSubTypesOf(root);
        subClasses.add(root);
        subClasses = filterInterface(interfaceToInherit, subClasses);
        return toClassModelList(subClasses);
    }

    private <R> Set<Class<? extends R>> filterInterface(final Class<?> interfaceToInherit,
            Set<Class<? extends R>> subClasses) {
        if (Objects.nonNull(interfaceToInherit)) {
            subClasses = subClasses.stream()
                    .filter((s) -> interfaceToInherit.isAssignableFrom(s)).collect(Collectors.toSet());
        }
        return subClasses;
    }

    private <R> ClassModelList toClassModelList(Set<Class<? extends R>> subClasses) {
        ClassModelList result = new ClassModelList();
        subClasses.forEach((s) -> {result.add(new ClassModel(s));});
        return result;
    }

    static ClassLoaderAdapter forRealm(
            final Class<?> classLoaderSeed, final ClassRealm realm) {
        return new ClassLoaderAdapterRealm(classLoaderSeed, realm);
    }

    static ClassLoaderAdapter forCl(final ClassLoader classLoader) {
        return new ClassLoaderAdapterCl(classLoader);
    }

    static private class ClassLoaderAdapterRealm extends ClassLoaderAdapter {
        private final Class<?> classLoaderSeed;
        private final ClassRealm realm;

        ClassLoaderAdapterRealm(
                final Class<?> classLoaderSeed,
                final ClassRealm realm) {
            this.classLoaderSeed = classLoaderSeed;
            this.realm = realm;
        }

        @Override
        Class<?> loadClass(final String name) throws ClassNotFoundException {
            return realm.loadClass(name);
        }

        @Override
        ClassLoader getClassLoader() {
            return realm.getImportClassLoader(classLoaderSeed.getName());
        }
    }

    static private class ClassLoaderAdapterCl extends ClassLoaderAdapter {
        private final ClassLoader cl;

        ClassLoaderAdapterCl(final ClassLoader cl) {
            this.cl = cl;
        }

        @Override
        Class<?> loadClass(final String name) throws ClassNotFoundException {
            return cl.loadClass(name);
        }

        @Override
        ClassLoader getClassLoader() {
            return cl;
        }
    }
}