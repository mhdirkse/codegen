package com.github.mhdirkse.codegen.plugin;

import java.util.Map;

import org.apache.velocity.VelocityContext;

import com.github.mhdirkse.codegen.plugin.model.ClassModel;

interface VelocityGenerator {
    void run(Map<String, ClassModel> variables);
    String getTemplateName();
    VelocityContext getVelocityContext();
    String getOutputClass();
}
