package com.github.mhdirkse.codegen.plugin;

import java.util.Map;

import org.antlr.v4.runtime.Token;

import com.github.mhdirkse.codegen.plugin.model.ClassModel;

class VelocityGeneratorAbstractImplementation extends VelocityGeneratorBase {
    VelocityGeneratorAbstractImplementation(
            final VelocityGeneratorVarReferences varReferences,
            final Token startToken,
            final CodegenListenerHelper helper) {
        super(varReferences, startToken, helper);
    }

    @Override
    public void run(final Map<String, ClassModel> variables) {
        super.run(variables);
        velocityContext.put("source", variables.get(varReferences.getSource()));
        velocityContext.put("nonVoidCommonReturnType", getNonVoidReturnType(checkCommonReturnType(variables)));
    }

    private String getNonVoidReturnType(final String commonReturnType) {
        String nonVoidCommonReturnType = null;
        if (!commonReturnType.equals("void")) {
            nonVoidCommonReturnType = commonReturnType;
        }
        return nonVoidCommonReturnType;
    }

    @Override
    public String getTemplateName() {
        return "abstractImplementationTemplate";
    }
}
