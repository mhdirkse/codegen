package com.github.mhdirkse.codegen.plugin.impl;

import java.util.function.Consumer;

import lombok.Setter;

class ServiceFactoryImpl extends ServiceFactory {
    private final ClassServiceImpl classService;

    ServiceFactoryImpl(
            final Runnable program,
            final StatusReportingService reporter,
            final ClassLoader cl) {
        super(program, reporter);
        this.classService = new ClassServiceImpl(this);
        this.classService.setCl(cl);
    }

    @Override
    protected ClassService classService() {
        return classService;
    }

    @Override
    protected FileWriteService fileWriteService() {
        return new FileWriteService() {
            @Override
            public void write(final FileContentsDefinition fcd, final Consumer<Exception> callback) {
            }
        };
    }

    private static final class ClassServiceImpl extends ClassService {
        @Setter
        private ClassLoader cl;

        ClassServiceImpl(ServiceFactory sf) {
            super(sf);
        }

        @Override
        protected Class<?> doLoadClass(String fullName) throws ClassNotFoundException {
            return cl.loadClass(fullName);
        }
    }
}
