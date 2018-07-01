package com.github.mhdirkse.codegen.plugin.model;

import java.util.List;

public class VelocityTask {
    private String templateName;
    private List<VelocityEntry> velocityEntries;
    private String outputClassName;

    public String getTemplateName() {
        return templateName;
    }
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    public List<VelocityEntry> getVelocityEntries() {
        return velocityEntries;
    }
    public void setVelocityEntries(List<VelocityEntry> velocityEntries) {
        this.velocityEntries = velocityEntries;
    }
    public String getOutputClassName() {
        return outputClassName;
    }
    public void setOutputClassName(String outputClassName) {
        this.outputClassName = outputClassName;
    }
}
