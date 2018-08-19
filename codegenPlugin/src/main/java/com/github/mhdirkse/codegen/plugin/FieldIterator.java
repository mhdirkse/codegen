package com.github.mhdirkse.codegen.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;
import org.reflections.ReflectionUtils;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;
import com.github.mhdirkse.codegen.compiletime.TypeHierarchy;

abstract class FieldIterator extends AnnotationManipulation {
    final Logger logger;
    final Runnable program;
    final Class<?> targetType;

    Set<Field> fields;
    Set<Field> fieldsAnyModifier;
    Set<Field> fieldsAnyType;
    Set<Field> fieldsAny;

    FieldIterator(
            final Runnable program,
            final Class<? extends Annotation> a,
            final Logger logger,
            final Class<?> targetType) {
        super(a, logger);
        this.logger = logger;
        this.program = program;
        this.targetType = targetType;
    }

    List<Optional<FieldManipulation>> run() {
        getFields();
        getFieldsAnyModifier();
        getFieldsAnyType();
        getFieldsAllWrong();
        checkAllFieldsPublic();
        checkAllFieldsCorrectType();
        checkNoFieldsAllWrong();
        return getFieldManipulations();
    }

    @SuppressWarnings("unchecked")
    private void getFields() {
        fields = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withTypeAssignableTo(targetType),
                ReflectionUtils.withAnnotation(annotation),
                ReflectionUtils.withModifier(Modifier.PUBLIC));
    }

    @SuppressWarnings("unchecked")
    private void getFieldsAnyModifier() {
        fieldsAnyModifier = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withTypeAssignableTo(targetType),
                ReflectionUtils.withAnnotation(annotation));
    }

    @SuppressWarnings("unchecked")
    private void getFieldsAnyType() {
        fieldsAnyType = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withAnnotation(annotation),
                ReflectionUtils.withModifier(Modifier.PUBLIC));
    }

    @SuppressWarnings("unchecked")
    private void getFieldsAllWrong() {
        fieldsAny = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withAnnotation(annotation));
    }

    private void checkAllFieldsPublic() {
        Set<Field> nonPublicInputFields = new HashSet<>(fieldsAnyModifier);
        nonPublicInputFields.removeAll(fields);
        nonPublicInputFields.forEach((f) -> error(f, "Field is not public."));
    }

    private void checkAllFieldsCorrectType() {
        Set<Field> nonClassModelInputFields = new HashSet<>(fieldsAnyType);
        nonClassModelInputFields.removeAll(fields);
        nonClassModelInputFields.forEach((f) -> error(f, String.format(
                "Field is not %s.", targetType.getSimpleName())));
    }

    private void checkNoFieldsAllWrong() {
        Set<Field> allWrongInputFields = new HashSet<>(fieldsAny);
        allWrongInputFields.removeAll(fields);
        allWrongInputFields.removeAll(fieldsAnyModifier);
        allWrongInputFields.removeAll(fieldsAnyType);
        allWrongInputFields.forEach((f) -> error(f, String.format(
                "Field is not public and not %s.", targetType.getSimpleName())));
    }

    private List<Optional<FieldManipulation>> getFieldManipulations() {
        List<Optional<FieldManipulation>> result = new ArrayList<>();
        result.addAll(fields.stream().map((f) -> getFieldManipulation(f))
                .collect(Collectors.toList()));
        result.addAll(fieldsAny.stream()
                .filter(((Predicate<Field>) fields::contains).negate())
                .map((f) -> Optional.<FieldManipulation>empty())
                .collect(Collectors.toList()));
        return result;
    }

    abstract Optional<FieldManipulation> getFieldManipulation(final Field f);

    static class InputPopulator extends FieldIterator {
        final ClassLoaderAdapter cla;

        InputPopulator(
                final Runnable program,
                final Logger logger,
                final ClassLoaderAdapter cla) {
            super(program, Input.class, logger, ClassModel.class);
            this.cla = cla;
        }

        @Override
        Optional<FieldManipulation> getFieldManipulation(final Field f) {
            return Optional.of((FieldManipulation) new FieldManipulation.InputPopulator(
                    annotation,
                    f,
                    logger,
                    program,
                    cla));
        }
    }

    static class OutputPopulator extends FieldIterator {
        OutputPopulator(
                final Runnable program,
                final Logger logger) {
            super(program, Output.class, logger, VelocityContext.class);
        }

        @Override
        Optional<FieldManipulation> getFieldManipulation(final Field f) {
            return Optional.of((FieldManipulation) new FieldManipulation.OutputPopulator(
                    f,
                    logger,
                    program));
        }        
    }

    static class HierarchyPopulator extends FieldIterator {
        final ClassLoaderAdapter cla;

        HierarchyPopulator(
                final Runnable program,
                final Logger logger,
                final ClassLoaderAdapter cla) {
            super(program, TypeHierarchy.class, logger, ClassModelList.class);
            this.cla = cla;
        }

        @Override
        Optional<FieldManipulation> getFieldManipulation(final Field f) {
            return Optional.of((FieldManipulation) new FieldManipulation.HierarchyPopulator(
                    f,
                    logger,
                    program,
                    cla));
        }
    }
}
