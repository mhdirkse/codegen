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

    <T extends Object> Optional<ClassModel> checkTargetIsClassModel(final T target, final Callback callback) {
        if(!(target instanceof ClassModel)) {
            Status status = callback.getStatusTargetTypeMismatch();
            sf.reporter().report(status);
            return Optional.empty();
        }
        else {
            return Optional.of((ClassModel) target);
        }
    }
}
