package com.github.mhdirkse.codegen.plugin.impl;

import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FILE_WRITE_IO_ERROR;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_GET_ERROR;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_GET_ERROR_AFTER_PROGRAM_RUN;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_MISSING_ACCESS_MODIFIER;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_REQUIRED_CLASS_NOT_FOUND;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_SET_ERROR;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_TYPE_MISMATCH;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_UNWANTED_ACCESS_MODIFIER;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_VELOCITY_CONTEXT_CLASS_MODEL_NO_FULL_NAME;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_VELOCITY_CONTEXT_LACKS_TARGET;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.FIELD_VELOCITY_CONTEXT_TARGET_NOT_CLASS_MODEL;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.UNKNOWN_FIELD_ERROR;
import static com.github.mhdirkse.codegen.plugin.impl.StatusCode.UNKONWN_FIELD_ERROR_AFTER_PROGRAM_RUN;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;
import com.github.mhdirkse.codegen.compiletime.TypeHierarchy;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;;

public class CodegenMojoDelegate
implements Runnable
{
    private final Runnable program;
    private final ServiceFactory sf;

    public CodegenMojoDelegate(final Runnable program, final ServiceFactory sf) 
    {
        this.program = program;
        this.sf = sf;
    }

    public boolean hasErrors() {
        return sf.reporter().hasErrors();
    }
    
    @Override
    public void run() {
        populate();
        if(!sf.reporter().hasErrors()) {
            program.run();
            writeOutputFiles();
        }
    }

    void populate() {
        populateInputs();
        populateOutputs();
        populateTypeHierarchies();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    private class InputDefinition {
        Field field;
        ClassModel classModel;
    }

    private void populateInputs() {
        getFields(Input.class, ClassModel.class).stream()
            .map(this::getInputDefinition)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(def -> {sf.fieldService().setField(
                    def.field, def.classModel, getCheckFieldCallback(
                            Input.class, def.field, ClassModel.class));});
    }

    private Set<Field> getFields(
            final Class<? extends Annotation> annotation,
            final Class<?> targetType) {
        Set<Field> publicWithRequestedAnnotationFields = sf.fieldLister().getFields(
                annotation, getFieldListerServiceCallback(annotation));
        return publicWithRequestedAnnotationFields.stream()
            .filter((f) -> checkField(annotation, f, targetType)).collect(Collectors.toSet());
    }

    private Boolean checkField(
            final Class<? extends Annotation> annotation,
            final Field field,
            final Class<?> targetType) {
        FieldService service = sf.fieldService();
        FieldService.Callback callback = getCheckFieldCallback(annotation, field, targetType);
        return BooleanUtils.and(new boolean[] {
                service.checkNotFinal(field, callback),
                service.checkNotStatic(field, callback),
                service.checkType(field, targetType, callback)});
    }

    private Optional<InputDefinition> getInputDefinition(final Field field) {
        InputDefinition result = new InputDefinition();
        result.field = field;
        String className = field.getAnnotation(Input.class).value();
        return sf.classService()
                .loadClass(className, getInputClassCallback(result.field, className))
                .map(ClassService.ClassAdapter::getAdaptee)
                .map(ClassModel::new)
                .map(result::setClassModel);
    }

    private void populateOutputs() {
        getFields(Output.class, VelocityContext.class)
            .forEach(f ->
                sf.fieldService().setField(f, new VelocityContext(), getOutputCallback(f)));
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    private class HierarchyDefinition {
        Field field;
        ClassModelList result;
    }

    private void populateTypeHierarchies() {
        getFields(TypeHierarchy.class, ClassModelList.class).stream()
            .map(this::getHierarchyDefinition)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(def -> sf.fieldService().setField(
                    def.field, def.result, getHierarchyCallback(def.field)));
    }

    private Optional<HierarchyDefinition> getHierarchyDefinition(final Field field) {
        HierarchyDefinition result = new HierarchyDefinition();
        result.field = field;
        TypeHierarchy annotation = field.getAnnotation(TypeHierarchy.class);
        String parentClass = annotation.value();
        Class<?> interfaceToInherit = getInterfaceToInherit(field, annotation.filterIsA());
        return sf.classService().loadClass(parentClass, getHierarchyClassCallback(field, parentClass))
            .map(c -> sf.classService().getHierarchy(c.getAdaptee(), interfaceToInherit))
            .map(result::setResult);
    }

    private Class<?> getInterfaceToInherit(final Field field, final String optionalName) {
        return Optional.ofNullable(optionalName)
                .filter(not(StringUtils::isBlank))
                .flatMap(n -> sf.classService().loadClass(
                    n, getHierarchyFilterCallback(field, n)))
                .map(ClassService.ClassAdapter::getAdaptee).orElse(null);
    }

    static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }

    void writeOutputFiles() {
        getFields(Output.class, VelocityContext.class).stream()
                .map(this::getFileContentsDefinition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(fcd -> sf.fileWriteService().write(fcd, getFileWriteErrorCallback(fcd)));
    }

    private Optional<FileContentsDefinition> getFileContentsDefinition(final Field field) {
        FileContentsDefinition result = new FileContentsDefinition();
        result.setTemplateFileName(field.getAnnotation(Output.class).value());
        Optional<FileContentsDefinition> resultWhenFilled = fillFcdWithVelocityContext(result, field);
        return fillFcdWithOutputClassName(resultWhenFilled, field);
    }

    private Optional<FileContentsDefinition> fillFcdWithVelocityContext(
            final FileContentsDefinition original,
            final Field field) {
        Optional<FileContentsDefinition> resultWhenFilled = sf.fieldService().getField(
                field, getFileContentsCallback(field))
            .map(o -> (VelocityContext) o)
            .map(original::setVelocityContext);
        return resultWhenFilled;
    }

    private Optional<FileContentsDefinition> fillFcdWithOutputClassName(
            Optional<FileContentsDefinition> resultWhenFilled, final Field field) {
        return resultWhenFilled
            .map(FileContentsDefinition::getVelocityContext)
            .flatMap(vc -> sf.velocityContextService().checkHasTarget(vc, getVelocityContextCallback(field)))
            .flatMap(o -> sf.velocityContextService().checkTargetIsClassModel(
                    o, getVelocityContextCallback(field)))
            .flatMap(cm -> sf.velocityContextService().checkClassModelHasFullName(
                    cm, getVelocityContextCallback(field)))
            .flatMap(className -> resultWhenFilled.map(
                    result -> result.setOutputClassName(className)));
    }

    private FieldListerService.Callback getFieldListerServiceCallback(
            final Class<? extends Annotation> annotation) {
        return new FieldListerService.Callback() {
            @Override
            public Status getStatusAccessModifierError(
                    final Field field,
                    final String accessModifier) {
                return Status.forFieldError(
                        FIELD_MISSING_ACCESS_MODIFIER,
                        annotation,
                        field,
                        accessModifier);
            }            
        };
    }

    private FieldService.Callback getCheckFieldCallback(
            final Class<? extends Annotation> annotation,
            final Field field,
            Class<?> targetType) {
        return new FieldService.Callback() {
            @Override
            public Status getStatusAccessModifierError(String modifier) {
                return Status.forFieldError(
                        FIELD_UNWANTED_ACCESS_MODIFIER,
                        annotation, field, modifier);
            }
    
            @Override
            public Status getStatusTypeMismatch(Class<?> actualType) {
                return Status.forFieldError(
                        FIELD_TYPE_MISMATCH,
                        annotation,
                        field,
                        targetType.getName(),
                        actualType.getName());
            }
    
            @Override
            public Status getStatusFieldSetError() {
                return Status.forFieldError(
                        FIELD_SET_ERROR,
                        annotation,
                        field);
            }
    
            @Override
            public Status getStatusFieldGetError() {
                return Status.forFieldError(
                        FIELD_GET_ERROR,
                        annotation,
                        field);
            }

            @Override
            public Status getStatusFieldValueIsNull() {
                return unknown();
            }

            private Status unknown() { 
                return Status.forFieldError(
                        UNKNOWN_FIELD_ERROR,
                        annotation,
                        field);
            }
        };
    }

    private ClassService.Callback getInputClassCallback(
            final Field field,
            final String className) {
        return new ClassService.Callback() {
            @Override
            public Status getStatusClassNotFound() {
                return Status.forFieldError(
                        FIELD_REQUIRED_CLASS_NOT_FOUND,
                        Input.class,
                        field,
                        className);
            }
        };
    }

    private FieldService.Callback getOutputCallback(final Field field) {
        return new FieldService.Callback() {
            @Override
            public Status getStatusAccessModifierError(String modifier) {
                return unknown();
            }

            @Override
            public Status getStatusTypeMismatch(Class<?> actual) {
                return unknown();
            }

            @Override
            public Status getStatusFieldGetError() {
                return unknown();
            }

            private Status unknown() {
                return Status.forFieldError(
                        UNKNOWN_FIELD_ERROR,
                        Output.class,
                        field);
            }

            @Override
            public Status getStatusFieldSetError() {
                return Status.forFieldError(
                        FIELD_SET_ERROR,
                        Output.class,
                        field);
            }

            @Override
            public Status getStatusFieldValueIsNull() {
                return unknown();
            }
        };
    }

    private FieldService.Callback getHierarchyCallback(final Field field) {
        return new FieldService.Callback() {
            @Override
            public Status getStatusTypeMismatch(Class<?> actual) {
                return unknown();
            }
            
            @Override
            public Status getStatusFieldGetError() {
                return unknown();
            }
            
            @Override
            public Status getStatusAccessModifierError(String modifier) {
                return unknown();
            }

            private Status unknown() {
                return Status.forFieldError(
                        UNKNOWN_FIELD_ERROR,
                        TypeHierarchy.class,
                        field);
            }

            @Override
            public Status getStatusFieldSetError() {
                return Status.forFieldError(
                        FIELD_SET_ERROR,
                        TypeHierarchy.class,
                        field);
            }

            @Override
            public Status getStatusFieldValueIsNull() {
                return unknown();
            }
        };
    }

    private ClassService.Callback getHierarchyClassCallback(
            final Field field, final String parentClass) {
        return new ClassService.Callback() {
            @Override
            public Status getStatusClassNotFound() {
                return Status.forFieldError(
                        FIELD_REQUIRED_CLASS_NOT_FOUND,
                        TypeHierarchy.class,
                        field,
                        parentClass);
            }
        };
    }

    private ClassService.Callback getHierarchyFilterCallback(final Field field, final String filterClass) {
        return new ClassService.Callback() {
            @Override
            public Status getStatusClassNotFound() {
                return Status.forFieldError(
                        FIELD_REQUIRED_CLASS_NOT_FOUND,
                        TypeHierarchy.class,
                        field,
                        filterClass);
            }
        };
    }

    private FieldService.Callback getFileContentsCallback(final Field field) {
        return new FieldService.Callback() {
            @Override
            public Status getStatusAccessModifierError(String modifier) {
                return unknown();
            }

            @Override
            public Status getStatusTypeMismatch(Class<?> actual) {
                return unknown();
            }

            @Override
            public Status getStatusFieldSetError() {
                return unknown();
            }

            private Status unknown() {
                return Status.forFieldError(
                        UNKONWN_FIELD_ERROR_AFTER_PROGRAM_RUN,
                        Output.class,
                        field);
            }
            @Override
            public Status getStatusFieldGetError() {
                return Status.forFieldError(
                        FIELD_GET_ERROR_AFTER_PROGRAM_RUN,
                        Output.class,
                        field);
                        
            }

            @Override
            public Status getStatusFieldValueIsNull() {
                return Status.forFieldError(
                        StatusCode.FIELD_GET_ERROR_AFTER_PROGRAM_RUN,
                        Output.class,
                        field);
            }
        };
    }

    private VelocityContextService.Callback getVelocityContextCallback(final Field field) {
        return new VelocityContextService.Callback() {
            @Override
            public Status getStatusVelocityContextEmpty() {
                return Status.forFieldError(
                        FIELD_GET_ERROR_AFTER_PROGRAM_RUN,
                        Output.class,
                        field);
            }

            @Override
            public Status getStatusVelocityContextLacksTarget() {
                return Status.forFieldError(
                        FIELD_VELOCITY_CONTEXT_LACKS_TARGET,
                        Output.class,
                        field);
            }

            @Override
            public Status getStatusTargetTypeMismatch() {
                return Status.forFieldError(
                        FIELD_VELOCITY_CONTEXT_TARGET_NOT_CLASS_MODEL,
                        Output.class,
                        field,
                        ClassModel.class.getName(),
                        field.getType().getName());
            }

            @Override
            public Status getStatusClassModelNoFullName() {
                return Status.forFieldError(
                        FIELD_VELOCITY_CONTEXT_CLASS_MODEL_NO_FULL_NAME,
                        Output.class,
                        field);
            }
        };
    }

    Consumer<Exception> getFileWriteErrorCallback(final FileContentsDefinition fcd) {
        return new Consumer<Exception>() {
            @Override
            public void accept(final Exception e) {
                Status status = Status.general(
                        FILE_WRITE_IO_ERROR,
                        LogPriority.ERROR);
                sf.reporter().report(status, e);
            }
        };
    }
}
