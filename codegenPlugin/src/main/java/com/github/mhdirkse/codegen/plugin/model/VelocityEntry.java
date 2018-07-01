package com.github.mhdirkse.codegen.plugin.model;

public class VelocityEntry {
    private String entryName;
    private ClassModel classModel;

    public String getEntryName() {
        return entryName;
    }
    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }
    public ClassModel getClassModel() {
        return classModel;
    }
    public void setClassModel(ClassModel classModel) {
        this.classModel = classModel;
    }
}
