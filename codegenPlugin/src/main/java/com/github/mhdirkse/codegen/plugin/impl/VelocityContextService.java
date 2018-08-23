package com.github.mhdirkse.codegen.plugin.impl;

import java.util.Objects;

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

    void checkNotEmpty(final VelocityContext velocityContext, final Callback callback) {
        if(Objects.isNull(velocityContext)) {
            Status status = callback.getStatusVelocityContextEmpty();
            sf.reporter().report(status);
        }
    }

    void checkHasTarget(final VelocityContext velocityContext, final Callback callback) {
        if(Objects.isNull(velocityContext.get("target"))) {
            Status status = callback.getStatusVelocityContextLacksTarget();
            sf.reporter().report(status);
        }
    }

    void checkTargetIsClassModel(final VelocityContext velocityContext, final Callback callback) {
        if(!(velocityContext.get("target") instanceof ClassModel)) {
            Status status = callback.getStatusTargetTypeMismatch();
            sf.reporter().report(status);
        }
    }
}
