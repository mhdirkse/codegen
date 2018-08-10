package com.github.mhdirkse.codegen.compiletime;

import java.util.ArrayList;

import org.apache.velocity.VelocityContext;

public final class VelocityContexts {
    private VelocityContexts() {
    }

    public static void populateChainContext(
            final ClassModel source,
            final String chainClassName,
            final String chainHandlerClassName,
            final VelocityContext velocityContext) {
        ChainClassModels models = getChainClassModels(source, chainClassName, chainHandlerClassName);
        velocityContext.put("source", source);
        velocityContext.put("target", models.chainModel);
        velocityContext.put("handler", models.chainHandlerModel);        
    }

    public static void populateChainHandlerContext(
            final ClassModel source,
            final String chainClassName,
            final String chainHandlerClassName,
            final VelocityContext velocityContext) {
        ChainClassModels models = getChainClassModels(source, chainClassName, chainHandlerClassName);
        velocityContext.put("source", source);
        velocityContext.put("target", models.chainHandlerModel);
    }

    private static ChainClassModels getChainClassModels(
            final ClassModel source,
            final String chainClassName,
            final String chainHandlerClassName) {
        ChainClassModels result = new ChainClassModels();
        ClassModel chainModel = new ClassModel();
        chainModel.setFullName(chainClassName);
        chainModel.setMethods(new ArrayList<>(source.getMethods()));
        chainModel.setReturnTypeForAllMethods("void");
        ClassModel chainHandlerModel = getChainHandlerClassModel(source, chainHandlerClassName);
        result.chainModel = chainModel;
        result.chainHandlerModel = chainHandlerModel;
        return result;
    }

    private static class ChainClassModels {
        ClassModel chainModel;
        ClassModel chainHandlerModel;
    }

    private static ClassModel getChainHandlerClassModel(final ClassModel source, final String chainHandlerClassName) {
        ClassModel chainHandlerModel = new ClassModel();
        chainHandlerModel.setFullName(chainHandlerClassName);
        chainHandlerModel.setMethods(new ArrayList<>(source.getMethods()));
        chainHandlerModel.setReturnTypeForAllMethods("boolean");
        chainHandlerModel.addParameterTypeToAllMethods(
                makeType("com.github.mhdirkse.codegen.runtime.HandlerStackContext", chainHandlerModel.getFullName()));
        return chainHandlerModel;
    }

    public static void populateChainAbstractHandlerContext(
            final ClassModel source,
            final String chainHandlerClassName,
            final String chainAbstractHandlerClassName,
            final VelocityContext velocityContext) {
        ClassModel chainHandlerModel = getChainHandlerClassModel(source, chainHandlerClassName);
        ClassModel chainAbstractHandlerModel = new ClassModel();
        chainAbstractHandlerModel.setFullName(chainAbstractHandlerClassName);
        chainAbstractHandlerModel.setMethods(new ArrayList<>(chainHandlerModel.getMethods()));
        velocityContext.put("source", chainHandlerModel);
        velocityContext.put("target", chainAbstractHandlerModel);
    }

    public static String makeType(final String base, final String typeParameter) {
        return base + "<" + typeParameter + ">";
    }
}
