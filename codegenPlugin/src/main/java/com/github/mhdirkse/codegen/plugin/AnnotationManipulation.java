package com.github.mhdirkse.codegen.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

abstract class AnnotationManipulation {
    final Class<? extends Annotation> annotation;
    final Logger logger;

    AnnotationManipulation(
            final Class<? extends Annotation> a,
            final Logger logger) {
        this.annotation = a;
        this.logger = logger;
    }

    public void debug(final Field f, final String msg) {
        logger.debug(format(f, msg));
    }

    public void info(final Field f, final String msg) {
        logger.info(format(f, msg));
    }

    String format(final Field f, final String msg) {
        return String.format("@%s %s: %s",
                annotation.getSimpleName(),
                f.getName(),
                msg);
    }

    public void error(final Field f, final String msg) {
        logger.error(format(f, msg));
    }

    public void debug(final Field f, final String msg, final Throwable e) {
        logger.debug(format(f, msg), e);
    }

    public void info(final Field f, final String msg, final Throwable e) {
        logger.info(format(f, msg), e);
    }

    public void error(final Field f, final String msg, final Throwable e) {
        logger.error(format(f, msg), e);
    }
}
