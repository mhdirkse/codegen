package com.github.mhdirkse.codegen.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.lang.StringUtils;

import com.github.mhdirkse.codegen.plugin.lang.CodegenBaseListener;
import com.github.mhdirkse.codegen.plugin.lang.CodegenParser;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;
import com.github.mhdirkse.codegen.plugin.model.VelocityEntry;
import com.github.mhdirkse.codegen.plugin.model.VelocityTask;
import com.google.common.collect.Iterables;

class CodegenListener extends CodegenBaseListener {
    private CodegenListenerHelper helper;
    private Map<String, ClassModel> variables = new HashMap<>();

    private ClassModel source = null;
    private ClassModel target = null;
    private ClassModel handler = null;

    private List<VelocityTask> tasks = new ArrayList<>();

    List<VelocityTask> getTasks() {
        return tasks;
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
        source = getVariable(ctx.getChild(0).getText(), ctx.getStart());
    }

    private ClassModel getVariable(final String variable, final Token startToken) {
        if (variables.containsKey(variable)) {
            return variables.get(variable);
        }
        else {
            throw new ParseCancellationException(getMessage(startToken, "Variable does not exist: " + variable));
        }
    }

    @Override
    public void enterTarget(@NotNull CodegenParser.TargetContext ctx) {
        target = getVariable(ctx.getChild(0).getText(), ctx.getStart());
    }

    @Override
    public void enterHandler(@NotNull CodegenParser.HandlerContext ctx) {
        handler = getVariable(ctx.getChild(0).getText(), ctx.getStart());
    }

    @Override
    public void exitChainStatement(@NotNull CodegenParser.ChainStatementContext ctx) {
        finishChain(target);
        finishChainHandler(handler);
        tasks.addAll(Arrays.asList(getVelocityTaskChain(), getVelocityTaskChainHandler()));
        clearClassModels();
    }

    private void finishChain(final ClassModel chainToFinish) {
        chainToFinish.setMethods(new ArrayList<>(source.getMethods()));
        // TODO: Set return type.
    }

    private void finishChainHandler(final ClassModel chainHandlerToFinish) {
        chainHandlerToFinish.setMethods(new ArrayList<>(source.getMethods()));
        chainHandlerToFinish.setReturnTypeForAllMethods("boolean");
        chainHandlerToFinish.addParameterTypeToAllMethods(
                makeType("com.github.mhdirkse.codegen.runtime.HandlerStackContext", chainHandlerToFinish.getFullName()));
    }

    private String makeType(final String base, final String typeParameter) {
        return base + "<" + typeParameter + ">";
    }

    private VelocityTask getVelocityTaskChain() {
        VelocityTask createChain = new VelocityTask();
        createChain.setTemplateName("delegatorClassTemplate");
        createChain.setVelocityEntries(new ArrayList<>(Arrays.asList(
                getVelocityEntrySource(),
                getVelocityEntryTarget(),
                getVelocityEntryHandler())));
        createChain.setOutputClassName(target.getFullName());
        return createChain;
    }

    private VelocityTask getVelocityTaskChainHandler() {
        VelocityTask createHandler = new VelocityTask();
        createHandler.setTemplateName("handlerInterfaceTemplate");
        createHandler.setVelocityEntries(new ArrayList<>(Arrays.asList(getVelocityEntryHandlerAsTarget())));
        createHandler.setOutputClassName(handler.getFullName());
        return createHandler;
    }

    private VelocityEntry getVelocityEntrySource() {
        VelocityEntry source = new VelocityEntry();
        source.setEntryName("source");
        source.setClassModel(this.source);
        return source;
    }

    private VelocityEntry getVelocityEntryTarget() {
        VelocityEntry target = new VelocityEntry();
        target.setEntryName("target");
        target.setClassModel(this.target);
        return target;
    }

    private VelocityEntry getVelocityEntryHandler() {
        VelocityEntry handler = new VelocityEntry();
        handler.setEntryName("handler");
        handler.setClassModel(this.handler);
        return handler;
    }

    private VelocityEntry getVelocityEntryCommonReturnType(final String commonReturnType) {
        VelocityEntry result = new VelocityEntry();
        result.setEntryName("nonVoidCommonReturnType");
        result.setProperty(getNonVoidReturnType(commonReturnType));
        return result;
    }

    private String getNonVoidReturnType(final String commonReturnType) {
        String nonVoidCommonReturnType = null;
        if (!commonReturnType.equals("void")) {
            nonVoidCommonReturnType = commonReturnType;
        }
        return nonVoidCommonReturnType;
    }

    private VelocityEntry getVelocityEntryHandlerAsTarget() {
        VelocityEntry handlerAsTarget = new VelocityEntry();
        handlerAsTarget.setEntryName("target");
        handlerAsTarget.setClassModel(this.handler);
        return handlerAsTarget;
    }

    private void clearClassModels() {
        source = null;
        target = null;
        handler = null;
    }

    @Override
    public void exitImplementStatement(@NotNull CodegenParser.ImplementStatementContext ctx) {
        String commonReturnType = checkCommonReturnType(ctx);
        finishImplementation(target, commonReturnType);
        tasks.add(getVelocityTaskImplementation(commonReturnType));
        clearClassModels();
    }

    private String checkCommonReturnType(@NotNull CodegenParser.ImplementStatementContext ctx) {
        Set<String> returnTypes = source.getReturnTypes();
        if (returnTypes.size() == 1) {
            return Iterables.getOnlyElement(returnTypes);
        }
        else {
            String msg = String.format("Methods of class %s have multiple return types: %s",
                    source.getFullName(),
                    StringUtils.join(returnTypes, ", "));
            Token start = ctx.getStart();
            helper.logError(start.getLine(), start.getCharPositionInLine(), msg);
            throw new ParseCancellationException(msg);
        }
    }

    private VelocityTask getVelocityTaskImplementation(final String commonReturnType) {
        VelocityTask createImplementation = new VelocityTask();
        createImplementation.setTemplateName("abstractImplementationTemplate");
        createImplementation.setVelocityEntries(new ArrayList<>(Arrays.asList(
                getVelocityEntrySource(),
                getVelocityEntryTarget(),
                getVelocityEntryCommonReturnType(commonReturnType))));
        createImplementation.setOutputClassName(target.getFullName());
        return createImplementation;
    }

    private void finishImplementation(final ClassModel implementationToFinish, final String commonReturnType) {
        implementationToFinish.setMethods(new ArrayList<>(source.getMethods()));
        implementationToFinish.setReturnTypeForAllMethods(commonReturnType);
    }
}
