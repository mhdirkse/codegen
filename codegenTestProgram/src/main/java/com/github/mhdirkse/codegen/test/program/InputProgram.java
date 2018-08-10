package com.github.mhdirkse.codegen.test.program;

import java.util.ArrayList;

import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.Output;
import com.github.mhdirkse.codegen.compiletime.VelocityContexts;
import com.github.mhdirkse.codegen.compiletime.ClassModel;

public class InputProgram implements Runnable {
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
        VelocityContexts.populateChainContext(
                source,
                MY_INTERFACE_DELEGATOR_FULL,
                MY_INTERFACE_HANDLER_FULL,
                chain);
        VelocityContexts.populateChainHandlerContext(
                source,
                MY_INTERFACE_DELEGATOR_FULL,
                MY_INTERFACE_HANDLER_FULL,
                chainHandler);
        VelocityContexts.populateChainAbstractHandlerContext(
                source,
                MY_INTERFACE_HANDLER_FULL,
                MY_INTERFACE_ABSTRACT_HANDLER_FULL,
                chainAbstractHandler);
    }
}
