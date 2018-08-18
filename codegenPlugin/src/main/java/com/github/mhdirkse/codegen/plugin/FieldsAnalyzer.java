package com.github.mhdirkse.codegen.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.reflections.ReflectionUtils;

class FieldsAnalyzer {
    private final Runnable program;
    private final Logger logger;

    Class<? extends Annotation> targetAnnotation;
    Class<?> targetType;

    Set<Field> fields;
    Set<Field> fieldsAnyModifier;
    Set<Field> fieldsAnyType;
    Set<Field> fieldsAny;
    
    FieldsAnalyzer(
            final Runnable program,
            final Logger logger) {
        this.program = program;
        this.logger = logger;
    }

    void run() throws MojoExecutionException {
        getFields();
        getFieldsAnyModifier();
        getFieldsAnyType();
        getFieldsAllWrong();
        checkAllFieldsPublic();
        checkAllFieldsCorrectType();
        checkNoFieldsAllWrong();
    }

    @SuppressWarnings("unchecked")
    private void getFields() {
        fields = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withTypeAssignableTo(targetType),
                ReflectionUtils.withAnnotation(targetAnnotation),
                ReflectionUtils.withModifier(Modifier.PUBLIC));
    }

    @SuppressWarnings("unchecked")
    private void getFieldsAnyModifier() {
        fieldsAnyModifier = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withTypeAssignableTo(targetType),
                ReflectionUtils.withAnnotation(targetAnnotation));
    }

    @SuppressWarnings("unchecked")
    private void getFieldsAnyType() {
        fieldsAnyType = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withAnnotation(targetAnnotation),
                ReflectionUtils.withModifier(Modifier.PUBLIC));
    }

    @SuppressWarnings("unchecked")
    private void getFieldsAllWrong() {
        fieldsAny = ReflectionUtils.getAllFields(
                program.getClass(),
                ReflectionUtils.withAnnotation(targetAnnotation));
    }

    private void checkAllFieldsPublic() throws MojoExecutionException {
        Set<Field> nonPublicInputFields = new HashSet<>(fieldsAnyModifier);
        nonPublicInputFields.removeAll(fields);
        if(!nonPublicInputFields.isEmpty()) {
            String msg = String.format("Some %s fields are not public: %s",
                    targetAnnotation.getSimpleName(),
                    StringUtils.join(getFieldNames(nonPublicInputFields), ", "));
            logger.error(msg);
            throw new MojoExecutionException(msg);
        }
    }

    private static List<String> getFieldNames(Collection<Field> fields) {
        List<String> result = new ArrayList<>();
        for (Field field : fields) {
            result.add(field.getName());
        }
        return result;
    }

    private void checkAllFieldsCorrectType() throws MojoExecutionException {
        Set<Field> nonClassModelInputFields = new HashSet<>(fieldsAnyType);
        nonClassModelInputFields.removeAll(fields);
        if(!nonClassModelInputFields.isEmpty()) {
            String msg = String.format("Some %s fields are not %s: %s",
                    targetAnnotation.getSimpleName(),
                    targetType.getSimpleName(),
                    StringUtils.join(getFieldNames(nonClassModelInputFields), ", "));
            logger.error(msg);
            throw new MojoExecutionException(msg);
        }
    }

    private void checkNoFieldsAllWrong() throws MojoExecutionException {
        Set<Field> allWrongInputFields = new HashSet<>(fieldsAny);
        allWrongInputFields.removeAll(fields);
        if(!allWrongInputFields.isEmpty()) {
            String msg = String.format("Some %s fields are not %s and not public: %s",
                    targetAnnotation.getSimpleName(),
                    targetType.getSimpleName(),
                    StringUtils.join(getFieldNames(allWrongInputFields), ", "));
            logger.error(msg);
            throw new MojoExecutionException(msg);
        }
    }
}
