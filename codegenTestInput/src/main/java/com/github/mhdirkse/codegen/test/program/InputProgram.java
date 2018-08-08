package com.github.mhdirkse.codegen.test.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

import com.github.mhdirkse.codegen.plugin.CodegenProgram;
import com.github.mhdirkse.codegen.plugin.Logger;
import com.github.mhdirkse.codegen.plugin.VelocityGenerator;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;

public class InputProgram implements CodegenProgram {
    private static final String OUTPUT_PACKAGE = "com.github.mhdirkse.codegen.test.output.";
    private static final String MY_INTERFACE = "MyInterface";
    private static final String MY_INTERFACE_DELEGATOR = "MyInterfaceDelegator";
    private static final String MY_INTERFACE_DELEGATOR_FULL = OUTPUT_PACKAGE + MY_INTERFACE_DELEGATOR;
    private static final String MY_INTERFACE_HANDLER = "MyInterfaceHandler";
    private static final String MY_INTERFACE_HANDLER_FULL = OUTPUT_PACKAGE + MY_INTERFACE_HANDLER;
    private static final String MY_INTERFACE_ABSTRACT_HANDLER = "MyInterfaceAbstractHandler";
    private static final String MY_INTERFACE_ABSTRACT_HANDLER_FULL = OUTPUT_PACKAGE + MY_INTERFACE_ABSTRACT_HANDLER;
    
    private List<VelocityGenerator> generators = new ArrayList<>();
    private Logger logger;

    @Override
    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public List<String> getSourceClasses() {
        return Arrays.asList(
                "com.github.mhdirkse.codegen.test.input.MyInterface",
                "com.github.mhdirkse.codegen.test.input.MyInterfaceForSuperGeneration");
    }

    @Override
    public void run(Map<String, ClassModel> variables) {
        handleChain(variables);
        handleAbstractImplement(variables);
    }

    void handleChain(Map<String, ClassModel> variables) {
        VelocityGeneratorVarReferences varReferencesChain = new VelocityGeneratorVarReferences();
        VelocityGeneratorVarReferences varReferencesHandler = new VelocityGeneratorVarReferences();
        ClassModel chainSource = variables.get(MY_INTERFACE);
        ClassModel chain = new ClassModel();
        chain.setFullName(MY_INTERFACE_DELEGATOR_FULL);
        chain.setMethods(new ArrayList<>(chainSource.getMethods()));
        chain.setReturnTypeForAllMethods("void");
        varReferencesChain.setSource(MY_INTERFACE);
        varReferencesChain.setTarget(MY_INTERFACE_DELEGATOR);
        varReferencesChain.setHandler(MY_INTERFACE_HANDLER);
        ClassModel chainHandler = new ClassModel();
        chainHandler.setFullName(MY_INTERFACE_HANDLER_FULL);
        chainHandler.setMethods(new ArrayList<>(chainSource.getMethods()));
        chainHandler.setReturnTypeForAllMethods("boolean");
        chainHandler.addParameterTypeToAllMethods(
                makeType("com.github.mhdirkse.codegen.runtime.HandlerStackContext", chainHandler.getFullName()));
        varReferencesHandler.setSource(MY_INTERFACE);
        varReferencesHandler.setTarget(MY_INTERFACE_HANDLER);
        generators.addAll(Arrays.asList(
                new VelocityGeneratorChain(varReferencesChain, getDummyToken(), logger),
                new VelocityGeneratorInterface(varReferencesHandler, getDummyToken(), logger)));
        variables.put(chain.getSimpleName(), chain);
        variables.put(chainHandler.getSimpleName(), chainHandler);
    }

    private String makeType(final String base, final String typeParameter) {
        return base + "<" + typeParameter + ">";
    }

    private Token getDummyToken() {
        return new Token() {

            @Override
            public String getText() {
                return null;
            }

            @Override
            public int getType() {
                return 0;
            }

            @Override
            public int getLine() {
                return 0;
            }

            @Override
            public int getCharPositionInLine() {
                return 0;
            }

            @Override
            public int getChannel() {
                return 0;
            }

            @Override
            public int getTokenIndex() {
                return 0;
            }

            @Override
            public int getStartIndex() {
                return 0;
            }

            @Override
            public int getStopIndex() {
                return 0;
            }

            @Override
            public TokenSource getTokenSource() {
                return null;
            }

            @Override
            public CharStream getInputStream() {
                return null;
            }            
        };
    }

    private void handleAbstractImplement(final Map<String, ClassModel> variables) {
        ClassModel source = variables.get(MY_INTERFACE_HANDLER);
        ClassModel target = new ClassModel();
        target.setFullName(MY_INTERFACE_ABSTRACT_HANDLER_FULL);
        target.setMethods(new ArrayList<>(source.getMethods()));
        VelocityGeneratorVarReferences varReferences = new VelocityGeneratorVarReferences();
        varReferences.setSource(MY_INTERFACE_HANDLER);
        varReferences.setTarget(MY_INTERFACE_ABSTRACT_HANDLER);
        generators.add(new VelocityGeneratorAbstractImplementation(varReferences, getDummyToken(), logger));
        variables.put(target.getSimpleName(), target);
    }

    @Override
    public List<VelocityGenerator> getGenerators() {
        return generators;
    }
}
