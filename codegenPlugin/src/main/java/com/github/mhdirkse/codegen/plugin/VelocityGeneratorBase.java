package com.github.mhdirkse.codegen.plugin;

import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.plugin.model.ClassModel;

abstract class VelocityGeneratorBase implements VelocityGenerator {
    final VelocityGeneratorVarReferences varReferences;
    final Token startToken;
    final CodegenListenerHelper helper;

    VelocityContext velocityContext;
    private String outputClass;

    VelocityGeneratorBase(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final CodegenListenerHelper helper) {
        this.varReferences = varReferences;
        this.startToken = startToken;
        this.helper = helper;
    }

    @Override
    public void run(final Map<String, ClassModel> variables) {
        velocityContext = new VelocityContext();
        velocityContext.put("source", variables.get(varReferences.getSource()));
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

    String checkCommonReturnType(Map<String, ClassModel> variables) {
        return helper.checkCommonReturnType(variables.get(varReferences.getSource()), startToken);
    }
}
