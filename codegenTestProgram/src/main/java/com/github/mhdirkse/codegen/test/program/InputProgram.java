package com.github.mhdirkse.codegen.test.program;

import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.compiletime.ClassModel;
import com.github.mhdirkse.codegen.compiletime.ClassModelList;
import com.github.mhdirkse.codegen.compiletime.Input;
import com.github.mhdirkse.codegen.compiletime.MethodModel;
import com.github.mhdirkse.codegen.compiletime.Output;
import com.github.mhdirkse.codegen.compiletime.TypeHierarchy;
import com.github.mhdirkse.codegen.compiletime.VelocityContexts;

public class InputProgram implements Runnable {
    private static final String OUTPUT_PACKAGE = "com.github.mhdirkse.codegen.test.output.";
    private static final String MY_INTERFACE_DELEGATOR = "MyInterfaceDelegator";
    private static final String MY_INTERFACE_DELEGATOR_FULL = OUTPUT_PACKAGE + MY_INTERFACE_DELEGATOR;
    private static final String MY_INTERFACE_HANDLER = "MyInterfaceHandler";
    private static final String MY_INTERFACE_HANDLER_FULL = OUTPUT_PACKAGE + MY_INTERFACE_HANDLER;
    private static final String MY_INTERFACE_ABSTRACT_HANDLER = "MyInterfaceAbstractHandler";
    private static final String MY_INTERFACE_ABSTRACT_HANDLER_FULL = OUTPUT_PACKAGE + MY_INTERFACE_ABSTRACT_HANDLER;
    private static final String HIERARCHY_FULL = "com.github.mhdirkse.codegen.test.output.HierarchyInterface";

    @Input("com.github.mhdirkse.codegen.test.input.MyInterface")
    public ClassModel source;

    @Output("delegatorClassTemplate")
    public VelocityContext chain;

    @Output("interfaceTemplate")
    public VelocityContext chainHandler;

    @Output("abstractImplementationTemplateReturningBoolean")
    public VelocityContext chainAbstractHandler;

    @TypeHierarchy("com.github.mhdirkse.codegen.test.input.Parent")
    public ClassModelList parentTypeHierarchy;

    @Output("interfaceTemplate")
    public VelocityContext hierarchy;

    @Override
    public void run() {
        chain();
        hierarchy();
    }

    private void chain() {
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

    private void hierarchy() {
        ClassModel outputInterface = new ClassModel();
        outputInterface.setFullName(HIERARCHY_FULL);
        outputInterface.setMethods(
            parentTypeHierarchy.stream()
                .map(this::getMethodModelOfArgumentClass)
                .collect(Collectors.toList()));
        hierarchy.put("target", outputInterface);
    }

    private MethodModel getMethodModelOfArgumentClass(final ClassModel argumentClass) {
        MethodModel m = new MethodModel();
        m.setName(getMethodName(argumentClass));
        m.setReturnType("void");
        return m;
    }

    private String getMethodName(final ClassModel argumentClass) {
        return "hierarchy" + argumentClass.getSimpleName();
    }
}
