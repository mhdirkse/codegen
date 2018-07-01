package com.github.mhdirkse.codegen.plugin.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClassModel {
    private String fullName;
    private List<MethodModel> methods;

    public ClassModel() {
        this.fullName = null;
        this.methods = null;
    }

    public ClassModel(final ClassModel other) {
        this.fullName = other.fullName;
        this.methods = null;
        if (other.methods != null) {
            this.methods = new ArrayList<>();
            for (MethodModel m : other.getMethods()) {
                this.methods.add(new MethodModel(m));
            }
        }
    }

    public String getFullName() {
        return fullName;
    }

    public String getSimpleName() {
        return fullName.substring(
                fullName.lastIndexOf('.') + 1, fullName.length());        
    }

    public String getPackage() {
        return fullName.substring(0, fullName.lastIndexOf('.'));        
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public List<MethodModel> getMethods() {
        return methods;
    }
    public void setMethods(List<MethodModel> methods) {
        this.methods = methods;
    }

    public void setMethods(Method[] reflectionMethods) {
        methods = new ArrayList<>();
        for (Method reflectionMethod : reflectionMethods) {
            methods.add(new MethodModel(reflectionMethod));
        }
    }

    public void addParameterTypeToAllMethods(final String parameterType) {
        for (MethodModel m : methods) {
            m.getParameterTypes().add(parameterType);
        }
    }

    public void setReturnTypeForAllMethods(final String returnType) {
        for (MethodModel m : methods) {
            m.setReturnType(returnType);
        }
    }
}