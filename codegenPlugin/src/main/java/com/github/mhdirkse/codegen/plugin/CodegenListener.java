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
import com.github.mhdirkse.codegen.plugin.model.VelocityEntry;
import com.github.mhdirkse.codegen.plugin.model.VelocityTask;

class CodegenListener extends CodegenBaseListener {
    private CodegenListenerHelper helper;
    private Map<String, ClassModel> variables = new HashMap<>();

    private ClassModel source = null;
    private ClassModel target = null;
    private ClassModel handler = null;

    private List<VelocityTask> tasks = new ArrayList<>();

    CodegenListener(final CodegenListenerHelper helper) {
        this.helper = helper;
    }

    @Override
    public void enterInputStatement(@NotNull CodegenParser.InputStatementContext ctx) {
        String fullName = ctx.getChild(0).getText();
        Token startToken = ctx.getStart();
        ClassModel classModel = addClassVariable(fullName, startToken);
        classModel.setMethods(helper.getMethods(classModel.getFullName()));
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
        return String.format("Line %d column %d: %s", tok.getLine(), tok.getCharPositionInLine(), text);
    }

    @Override
    public void enterOutputStatement(@NotNull CodegenParser.OutputStatementContext ctx) {
        String fullName = ctx.getChild(0).getText();
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
        VelocityTask createChain = new VelocityTask();
        createChain.setTemplateName("delegatorClassTemplate");
        VelocityEntry source = new VelocityEntry();
        source.setEntryName("source");
        source.setClassModel(this.source);
        VelocityEntry target = new VelocityEntry();
        target.setEntryName("target");
        target.setClassModel(this.target);
        VelocityEntry handler = new VelocityEntry();
        handler.setEntryName("handler");
        handler.setClassModel(this.handler);
        createChain.setVelocityEntries(new ArrayList<>(Arrays.asList(source, target, handler)));
        VelocityTask createHandler = new VelocityTask();
        createHandler.setTemplateName("handlerInterfaceTemplate");
        VelocityEntry handlerAsTarget = new VelocityEntry();
        handlerAsTarget.setEntryName("target");
        handlerAsTarget.setClassModel(this.handler);
        createHandler.setVelocityEntries(new ArrayList<>(Arrays.asList(handlerAsTarget)));
        tasks.addAll(Arrays.asList(createChain, createHandler));
        clearClassModels();
    }

    private void clearClassModels() {
        source = null;
        target = null;
        handler = null;
    }

    @Override
    public void exitImplementStatement(@NotNull CodegenParser.ImplementStatementContext ctx) {
        VelocityTask createImplementation = new VelocityTask();
        createImplementation.setTemplateName("abstractHandlerClassTemplate");
        VelocityEntry source = new VelocityEntry();
        source.setEntryName("source");
        source.setClassModel(this.source);
        VelocityEntry target = new VelocityEntry();
        target.setEntryName("target");
        target.setClassModel(this.target);
        createImplementation.setVelocityEntries(new ArrayList<>(Arrays.asList(source, target)));
        tasks.add(createImplementation);
        clearClassModels();
    }
}
