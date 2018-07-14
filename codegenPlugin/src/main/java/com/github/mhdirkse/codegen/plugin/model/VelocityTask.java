package com.github.mhdirkse.codegen.plugin.model;

import java.util.ArrayList;
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
        return copyVelocityEntries(velocityEntries);
    }

    private List<VelocityEntry> copyVelocityEntries(final List<VelocityEntry> sourceEntries) {
        List<VelocityEntry> result = null;
        if (sourceEntries != null) {
            result = new ArrayList<>();
            for (VelocityEntry entry : sourceEntries) {
                result.add(new VelocityEntry(entry));
            }
        }
        return result;
    }

    public void setVelocityEntries(List<VelocityEntry> velocityEntries) {
        this.velocityEntries = copyVelocityEntries(velocityEntries);
    }

    public String getOutputClassName() {
        return outputClassName;
    }

    public void setOutputClassName(String outputClassName) {
        this.outputClassName = outputClassName;
    }
}
