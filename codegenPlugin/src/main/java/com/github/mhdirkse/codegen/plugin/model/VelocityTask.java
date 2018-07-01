package com.github.mhdirkse.codegen.plugin.model;

import java.util.List;

public class VelocityTask {
    private String templateName;
    private List<VelocityEntry> velocityEntries;

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
}
