package com.github.mhdirkse.codegen.test.program;

import java.util.ArrayList;

import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.annotations.Input;
import com.github.mhdirkse.codegen.annotations.Output;
import com.github.mhdirkse.codegen.plugin.CodegenProgram;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;

public class InputProgram implements CodegenProgram {
    private static final String OUTPUT_PACKAGE = "com.github.mhdirkse.codegen.test.output.";
    private static final String MY_INTERFACE_DELEGATOR = "MyInterfaceDelegator";
    private static final String MY_INTERFACE_DELEGATOR_FULL = OUTPUT_PACKAGE + MY_INTERFACE_DELEGATOR;
    private static final String MY_INTERFACE_HANDLER = "MyInterfaceHandler";
    private static final String MY_INTERFACE_HANDLER_FULL = OUTPUT_PACKAGE + MY_INTERFACE_HANDLER;
    private static final String MY_INTERFACE_ABSTRACT_HANDLER = "MyInterfaceAbstractHandler";
    private static final String MY_INTERFACE_ABSTRACT_HANDLER_FULL = OUTPUT_PACKAGE + MY_INTERFACE_ABSTRACT_HANDLER;
    
    @Input("com.github.mhdirkse.codegen.test.input.MyInterface")
    public ClassModel source;

    @Output("delegatorClassTemplate")
    public VelocityContext chain;

    @Output("interfaceTemplate")
    public VelocityContext chainHandler;

    @Output("abstractImplementationTemplateReturningBoolean")
    public VelocityContext chainAbstractHandler;

    @Override
    public void run() {
        ClassModel chainModel = new ClassModel();
        chainModel.setFullName(MY_INTERFACE_DELEGATOR_FULL);
        chainModel.setMethods(new ArrayList<>(source.getMethods()));
        chainModel.setReturnTypeForAllMethods("void");
        ClassModel chainHandlerModel = new ClassModel();
        chainHandlerModel.setFullName(MY_INTERFACE_HANDLER_FULL);
        chainHandlerModel.setMethods(new ArrayList<>(source.getMethods()));
        chainHandlerModel.setReturnTypeForAllMethods("boolean");
        chainHandlerModel.addParameterTypeToAllMethods(
                makeType("com.github.mhdirkse.codegen.runtime.HandlerStackContext", chainHandlerModel.getFullName()));
        ClassModel chainAbstractHandlerModel = new ClassModel();
        chainAbstractHandlerModel.setFullName(MY_INTERFACE_ABSTRACT_HANDLER_FULL);
        chainAbstractHandlerModel.setMethods(new ArrayList<>(chainHandlerModel.getMethods()));
        chain.put("source", source);
        chain.put("target", chainModel);
        chain.put("handler", chainHandlerModel);
        chainHandler.put("source", source);
        chainHandler.put("target", chainHandlerModel);
        chainAbstractHandler.put("source", chainHandlerModel);
        chainAbstractHandler.put("target", chainAbstractHandlerModel);
    }

    private String makeType(final String base, final String typeParameter) {
        return base + "<" + typeParameter + ">";
    }
}
