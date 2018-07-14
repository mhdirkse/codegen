package com.github.mhdirkse.codegen.plugin.model;

public class VelocityEntry {
    private String entryName;
    private ClassModel classModel;
    private String property;

    public VelocityEntry() {
        entryName = null;
        classModel = null;
        property = null;
    }

    public VelocityEntry(final VelocityEntry other) {
        this.entryName = other.entryName;
        this.classModel = null;
        if (other.classModel != null) {
            this.classModel = new ClassModel(other.getClassModel());
        }
        this.property = other.property;
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

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    public Object getObject() {
        if ((classModel != null) && (property != null)) {
            throw new IllegalArgumentException("Cannot have both classModel and property filled: " + toString());
        } else if(classModel != null) {
            return getClassModel();
        } else if (property != null) {
            return property;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("entry name: %s, classModel: %s, property %s",
                entryName, classModel == null ? null : classModel.toString(), property);
    }
}
