package com.github.mhdirkse.codegen.test.program;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.github.mhdirkse.codegen.plugin.CodegenProgram;
import com.github.mhdirkse.codegen.plugin.Logger;
import com.github.mhdirkse.codegen.plugin.model.ClassModel;
import com.github.mhdirkse.codegen.test.input.MyInterface;

public class InputProgramTest implements Logger {
    @Override
    public void info(String msg) {
        System.out.println("INFO: " + msg);
    }

    @Override
    public void error(String msg) {
        System.out.println("ERROR: " + msg);
    }

    private CodegenProgram instance;
    private Map<String, ClassModel> variables;
    
    @Before
    public void setUp() {
        instance = new InputProgram();
        instance.setLogger(this);
        variables = new HashMap<>();
        ClassModel source = new ClassModel();
        source.setFullName(MyInterface.class.getName());
        source.setMethods(MyInterface.class.getMethods());
        variables.put(source.getSimpleName(), source);
    }

    @Test
    public void test() {
        instance.run(variables);
    }
}    
