package com.github.mhdirkse.codegen.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;
import com.github.mhdirkse.codegen.compiletime.TypeHierarchy;

abstract class FieldManipulation extends AnnotationManipulation implements Logger {
    final Field f;
    Object instantiatedObject = null;

    FieldManipulation(
            final Class<? extends Annotation> a,
            final Field f,
            final Logger logger) {
        super(a, logger);
        this.f = f;
    }

    public boolean run() {
        debug("Start populating...");
        try {
            boolean success = runImpl();
            if(success) {
                debug("Populating succeeded.");
            } else {
                error("Populating failed.");
            }
            return success;
        } catch(Throwable e) {
            error("Exception encountered while populating", e);
            return false;
        }
    }

    abstract boolean runImpl() throws Exception;

    @Override
    public void debug(final String msg) {
        super.debug(f, msg);
    }

    @Override
    public void info(final String msg) {
        super.info(f, msg);
    }

    @Override
    public void error(final String msg) {
        super.error(f, msg);
    }

    @Override
    public void debug(final String msg, final Throwable e) {
        super.debug(f,  msg, e);
    }

    @Override
    public void info(final String msg, final Throwable e) {
        super.info(f, msg, e);
    }

    @Override
    public void error(final String msg, final Throwable e) {
        super.error(f, msg, e);
    }

    static class InputPopulator extends FieldManipulation {
        private final Runnable program;
        private final ClassLoaderAdapter cla;
        
        InputPopulator(
                final Class<? extends Annotation> a,
                final Field f,
                final Logger logger,
                final Runnable program,
                final ClassLoaderAdapter cla) {
            super(a, f, logger);
            this.program = program;
            this.cla = cla;
        }

        @Override
        boolean runImpl() {
            Input annotationValue = f.getAnnotation(Input.class);
            String source = annotationValue.value();
            Class<?> clazz = null;
            try {
                clazz = cla.loadClass(source);
            } catch(ClassNotFoundException e) {
                error(String.format("Class not found: %s", source));
                return false;
            }
            try {
                instantiatedObject = new ClassModel(clazz);
                f.set(program, instantiatedObject);
                return true;
            } catch(IllegalAccessException e) {
                error(String.format("Could not populate field %s with class %s",
                        f.getName(), source));
                return false;
            }
        }
    }

    static class OutputPopulator extends FieldManipulation {
        private final Runnable program;

        OutputPopulator(
                final Field f,
                final Logger logger,
                final Runnable program) {
            super(Output.class, f, logger);
            this.program = program;
        }

        @Override
        boolean runImpl() {
            try {
                instantiatedObject = new VelocityContext();
                f.set(program, instantiatedObject);
                return true;
            } catch(IllegalAccessException e) {
                error("Could not populate field with VelocityContext: " + f.getName(), e);
                return false;
            }
        }
    }

    static class HierarchyPopulator extends FieldManipulation {
        final Runnable program;
        final ClassLoaderAdapter cla;

        HierarchyPopulator(
                final Field f,
                final Logger logger,
                final Runnable program,
                final ClassLoaderAdapter cla) {
            super(TypeHierarchy.class, f, logger);
            this.program = program;
            this.cla = cla;
        }

        @Override
        boolean runImpl() {
            TypeHierarchy annotationValue = f.getAnnotation(TypeHierarchy.class);
            String root = annotationValue.value();
            String optionalFilterClass = annotationValue.filterIsA();
            Class<?> rootClass = checkedLoadClass(root, "Root class of type hierarchy not available:");
            if (Objects.isNull(rootClass)) {
                return false;
            }
            Class<?> filterClass = null;
            if(!StringUtils.isBlank(optionalFilterClass)) {
                filterClass = checkedLoadClass(optionalFilterClass, "Class of filterIsA is not available:");
                if(Objects.isNull(filterClass)) {
                    return false;
                }
            }
            try {
                instantiatedObject = cla.getHierarchy(rootClass, filterClass);
                f.set(program, instantiatedObject);
                return true;
            } catch(IllegalAccessException e) {
                error(f, "Could not set hierarchy");
                return false;
            }
        }

        private Class<?> checkedLoadClass(final String className, final String messageIfNotFound) {
            Class<?> clazz = null;
            try {
                clazz = cla.loadClass(className);
            } catch(ClassNotFoundException e) {
                error(f, messageIfNotFound + ": " + className);
            }
            return clazz;
        }
    }
}
