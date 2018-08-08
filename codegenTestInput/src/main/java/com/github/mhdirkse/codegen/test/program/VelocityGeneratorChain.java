package com.github.mhdirkse.codegen.test.program;

import java.util.Map;

import org.antlr.v4.runtime.Token;

import com.github.mhdirkse.codegen.plugin.Logger;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;

class VelocityGeneratorChain extends VelocityGeneratorBase {
    VelocityGeneratorChain(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final Logger logger) {
        super(varReferences, startToken, logger);
    }

    @Override
    public void run(final Map<String, ClassModel> variables) {
        super.run(variables);
        velocityContext.put("source", variables.get(varReferences.getSource()));
        velocityContext.put("handler", variables.get(varReferences.getHandler()));
    }

    @Override
    public String getTemplateName() {
        return "delegatorClassTemplate";
    }
}
