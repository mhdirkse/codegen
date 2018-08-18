package com.github.mhdirkse.codegen.plugin;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

abstract class ClassLoaderAdapter {
    abstract Class<?> loadClass(final String name) throws ClassNotFoundException;
    abstract ClassLoader getClassLoader();

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
