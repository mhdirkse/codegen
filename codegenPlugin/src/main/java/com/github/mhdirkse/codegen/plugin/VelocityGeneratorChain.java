package com.github.mhdirkse.codegen.plugin;

import java.util.Map;

import org.antlr.v4.runtime.Token;

import com.github.mhdirkse.codegen.plugin.model.ClassModel;

class VelocityGeneratorChain extends VelocityGeneratorBase {
    VelocityGeneratorChain(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final CodegenListenerHelper helper) {
        super(varReferences, startToken, helper);
    }

    @Override
    public void run(final Map<String, ClassModel> variables) {
        super.run(variables);
        velocityContext.put("handler", variables.get(varReferences.getHandler()));
    }

    @Override
    public String getTemplateName() {
        return "delegatorClassTemplate";
    }
}
