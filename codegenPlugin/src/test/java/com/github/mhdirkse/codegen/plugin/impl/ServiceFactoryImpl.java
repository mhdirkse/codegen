package com.github.mhdirkse.codegen.plugin.impl;

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
    ClassService classService() {
        return classService;
    }

    @Override
    FileWriteService fileWriteService() {
        return new FileWriteService() {
            @Override
            void write(FileContentsDefinition fcd) {
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
        Class<?> doLoadClass(String fullName) throws ClassNotFoundException {
            return cl.loadClass(fullName);
        }
    }
}
