package com.github.mhdirkse.codegen.test.program;

import org.antlr.v4.runtime.Token;

import com.github.mhdirkse.codegen.plugin.Logger;

class VelocityGeneratorInterface extends VelocityGeneratorBase {
    VelocityGeneratorInterface(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final Logger logger) {
        super(varReferences, startToken, logger);
    }

    @Override
    public String getTemplateName() {
        return "interfaceTemplate";
    }
}
