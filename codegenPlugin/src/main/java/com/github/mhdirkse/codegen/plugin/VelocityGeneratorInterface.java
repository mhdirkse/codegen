package com.github.mhdirkse.codegen.plugin;

import org.antlr.v4.runtime.Token;

class VelocityGeneratorInterface extends VelocityGeneratorBase {
    VelocityGeneratorInterface(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final CodegenListenerHelper helper) {
        super(varReferences, startToken, helper);
    }

    @Override
    public String getTemplateName() {
        return "interfaceTemplate";
    }
}