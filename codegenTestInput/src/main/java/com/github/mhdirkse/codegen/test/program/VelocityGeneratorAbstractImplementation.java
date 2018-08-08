package com.github.mhdirkse.codegen.test.program;

import java.util.Map;

import org.antlr.v4.runtime.Token;

import com.github.mhdirkse.codegen.plugin.Logger;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;

class VelocityGeneratorAbstractImplementation extends VelocityGeneratorBase {
    VelocityGeneratorAbstractImplementation(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final Logger logger) {
        super(varReferences, startToken, logger);
    }

    @Override
    public void run(final Map<String, ClassModel> variables) {
        super.run(variables);
        velocityContext.put("source", variables.get(varReferences.getSource()));
    }

    @Override
    public String getTemplateName() {
        return "abstractImplementationTemplateReturningBoolean";
    }
}
