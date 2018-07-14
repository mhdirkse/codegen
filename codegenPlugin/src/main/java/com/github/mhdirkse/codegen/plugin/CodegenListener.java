package com.github.mhdirkse.codegen.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import com.github.mhdirkse.codegen.plugin.lang.CodegenBaseListener;
import com.github.mhdirkse.codegen.plugin.lang.CodegenParser;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;

class CodegenListener extends CodegenBaseListener {
    private CodegenListenerHelper helper;
    private Map<String, ClassModel> variables = new HashMap<>();
    private List<VelocityGenerator> velocityGenerators = new ArrayList<>();

    VelocityGeneratorVarReferences varReferences = new VelocityGeneratorVarReferences();

    List<VelocityGenerator> getVelocityGenerators() {
        return velocityGenerators;
    }

    Map<String, ClassModel> getVariables() {
        return variables;
    }

    CodegenListener(final CodegenListenerHelper helper) {
        this.helper = helper;
    }

    @Override
    public void enterInputStatement(@NotNull CodegenParser.InputStatementContext ctx) {
        String fullName = ctx.getChild(1).getText();
        Token startToken = ctx.getStart();
        ClassModel classModel = addClassVariable(fullName, startToken);
        try {
            classModel.setMethods(helper.getMethods(classModel.getFullName()));
        }
        catch (ClassNotFoundException e) {
            Token start = ctx.getStart();
            helper.logError(start.getLine(), start.getCharPositionInLine(), "Could not find class: " + fullName);
            throw new ParseCancellationException(e);
        }
    }

    private ClassModel addClassVariable(String fullName, Token startToken) {
        ClassModel classModel = new ClassModel();
        classModel.setFullName(fullName);
        if (variables.containsKey(classModel.getSimpleName())) {
            throw new ParseCancellationException(
                    getMessage(startToken, String.format("Variable already exists: %s", classModel.getSimpleName())));
        } else {
            variables.put(classModel.getSimpleName(), classModel);
        }
        return classModel;
    }

    private String getMessage(final Token tok, final String text) {
        return Utils.getErrorMessage(tok.getLine(), tok.getCharPositionInLine(), text);
    }

    @Override
    public void enterOutputStatement(@NotNull CodegenParser.OutputStatementContext ctx) {
        String fullName = ctx.getChild(1).getText();
        Token startToken = ctx.getStart();
        addClassVariable(fullName, startToken);
    }

    @Override
    public void enterSource(@NotNull CodegenParser.SourceContext ctx) {
        varReferences.setSource(ctx.getChild(0).getText());
    }

    @Override
    public void enterTarget(@NotNull CodegenParser.TargetContext ctx) {
        varReferences.setTarget(getVariable(ctx.getChild(0).getText(), ctx.getStart()));
    }

    private String getVariable(final String variable, final Token startToken) {
        if (variables.containsKey(variable)) {
            return variable;
        }
        else {
            throw new ParseCancellationException(getMessage(startToken, "Variable does not exist: " + variable));
        }
    }

    @Override
    public void enterHandler(@NotNull CodegenParser.HandlerContext ctx) {
        varReferences.setHandler(getVariable(ctx.getChild(0).getText(), ctx.getStart()));
    }

    @Override
    public void exitChainStatement(@NotNull CodegenParser.ChainStatementContext ctx) {
        finishChain();
        finishChainHandler();
        VelocityGeneratorVarReferences handlerReferences = getVarReferencesHandler();
        velocityGenerators.addAll(Arrays.asList(
                new VelocityGeneratorChain(varReferences, ctx.getStart(), helper),
                new VelocityGeneratorHandler(handlerReferences, ctx.getStart(), helper)));
        varReferences = new VelocityGeneratorVarReferences();
    }

    private void finishChain() {
        ClassModel source = variables.get(varReferences.getSource());
        ClassModel target = variables.get(varReferences.getTarget());
        target.setMethods(new ArrayList<>(source.getMethods()));
        target.setReturnTypeForAllMethods("void");
    }

    private void finishChainHandler() {
        ClassModel source = variables.get(varReferences.getSource());
        ClassModel handler = variables.get(varReferences.getHandler());
        handler.setMethods(new ArrayList<>(source.getMethods()));
        handler.setReturnTypeForAllMethods("boolean");
        handler.addParameterTypeToAllMethods(
                makeType("com.github.mhdirkse.codegen.runtime.HandlerStackContext", handler.getFullName()));
    }

    private VelocityGeneratorVarReferences getVarReferencesHandler() {
        VelocityGeneratorVarReferences handlerReferences = new VelocityGeneratorVarReferences();
        handlerReferences.setSource(varReferences.getSource());
        handlerReferences.setTarget(varReferences.getHandler());
        return handlerReferences;
    }

    private String makeType(final String base, final String typeParameter) {
        return base + "<" + typeParameter + ">";
    }

    @Override
    public void exitImplementStatement(@NotNull CodegenParser.ImplementStatementContext ctx) {
        String commonReturnType = helper.checkCommonReturnType(
                variables.get(varReferences.getSource()), ctx.getStart());
        finishImplementation(commonReturnType);
        velocityGenerators.add(new VelocityGeneratorAbstractImplementation(varReferences, ctx.start, helper));
        varReferences = new VelocityGeneratorVarReferences();
    }

    private void finishImplementation(final String commonReturnType) {
        ClassModel source = variables.get(varReferences.getSource());
        ClassModel target = variables.get(varReferences.getTarget());
        target.setMethods(new ArrayList<>(source.getMethods()));
        target.setReturnTypeForAllMethods(commonReturnType);
    }
}
