package com.github.mhdirkse.codegen.test.program;

import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.plugin.Logger;
import com.github.mhdirkse.codegen.plugin.VelocityGenerator;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;

abstract class VelocityGeneratorBase implements VelocityGenerator {
    final VelocityGeneratorVarReferences varReferences;
    final Token startToken;
    final Logger logger;

    VelocityContext velocityContext;
    private String outputClass;

    VelocityGeneratorBase(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final Logger logger) {
        this.varReferences = varReferences;
        this.startToken = startToken;
        this.logger = logger;
    }

    @Override
    public void run(final Map<String, ClassModel> variables) {
        velocityContext = new VelocityContext();
        ClassModel targetClassModel = variables.get(varReferences.getTarget());
        velocityContext.put("target", targetClassModel);
        outputClass = targetClassModel.getFullName();
    }

    @Override
    public final VelocityContext getVelocityContext() {
        return velocityContext;
    }

    @Override
    public final String getOutputClass() {
        return outputClass;
    }
}
