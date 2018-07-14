package com.github.mhdirkse.codegen.plugin.model;

public class VelocityEntry {
    private String entryName;
    private ClassModel classModel;

    public VelocityEntry() {
        entryName = null;
        classModel = null;
    }

    public VelocityEntry(final VelocityEntry other) {
        this.entryName = other.entryName;
        this.classModel = new ClassModel(other.getClassModel());
    }

    public String getEntryName() {
        return entryName;
    }
    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }
    public ClassModel getClassModel() {
        return new ClassModel(classModel);
    }
    public void setClassModel(ClassModel classModel) {
        this.classModel = new ClassModel(classModel);
    }
}
