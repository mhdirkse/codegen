package com.github.mhdirkse.codegen.plugin;

import java.util.List;
import java.util.Map;

import com.github.mhdirkse.codegen.plugin.model.ClassModel;

public interface CodegenProgram {
    void setLogger(Logger logger);
    List<String> getSourceClasses();
    void run(Map<String, ClassModel> variables);
    List<VelocityGenerator> getGenerators();
}
