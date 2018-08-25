package com.github.mhdirkse.codegen.plugin.impl;

import java.util.Objects;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.compiletime.ClassModel;

class VelocityContextService {
    static interface Callback {
        Status getStatusVelocityContextEmpty();
        Status getStatusVelocityContextLacksTarget();
        Status getStatusTargetTypeMismatch();
        Status getStatusClassModelNoFullName();
    }

    private final ServiceFactory sf;

    VelocityContextService(final ServiceFactory sf) {
        this.sf = sf;
    }

    Optional<VelocityContext> checkNotEmpty(final VelocityContext velocityContext, final Callback callback) {
        if(Objects.isNull(velocityContext)) {
            Status status = callback.getStatusVelocityContextEmpty();
            sf.reporter().report(status);
        }
        return Optional.ofNullable(velocityContext);
    }

    Optional<Object> checkHasTarget(final VelocityContext velocityContext, final Callback callback) {
        Object targetUnwrapped = velocityContext.get("target");
        if(Objects.isNull(targetUnwrapped)) {
            Status status = callback.getStatusVelocityContextLacksTarget();
            sf.reporter().report(status);
        }
        return Optional.ofNullable(targetUnwrapped);
    }

    Optional<ClassModel> checkTargetIsClassModel(final Object target, final Callback callback) {
        if(!(target instanceof ClassModel)) {
            Status status = callback.getStatusTargetTypeMismatch();
            sf.reporter().report(status);
            return Optional.<ClassModel>empty();
        }
        else {
            return Optional.of((ClassModel) target);
        }
    }

    Optional<String> checkClassModelHasFullName(final ClassModel cm, final Callback callback) {
        if(Objects.isNull(cm.getFullName())) {
            Status status = callback.getStatusClassModelNoFullName();
            sf.reporter().report(status);
            return Optional.<String>empty();
        } else {
            return Optional.of(cm.getFullName());
        }
    }
}
