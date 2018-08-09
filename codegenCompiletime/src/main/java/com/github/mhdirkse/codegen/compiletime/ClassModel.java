package com.github.mhdirkse.codegen.compiletime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassModel {
    private String fullName;
    private List<MethodModel> methods;
    private String superClass;

    public ClassModel() {
        this.fullName = null;
        this.methods = null;
    }

    public ClassModel(final ClassModel other) {
        this.fullName = other.fullName;
        this.methods = copyMethods(other.methods);
    }

    private static List<MethodModel> copyMethods(final List<MethodModel> source) {
        List<MethodModel> result = null;
        if (source != null) {
            result = new ArrayList<>();
            for (MethodModel method : source) {
                result.add(new MethodModel(method));
            }
        }
        return result;
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
        return copyMethods(methods);
    }

    public void setMethods(List<MethodModel> methods) {
        this.methods = copyMethods(methods);
    }

    public void setMethods(Method[] reflectionMethods) {
        methods = new ArrayList<>();
        for (Method reflectionMethod : reflectionMethods) {
            methods.add(new MethodModel(reflectionMethod));
        }
    }

    public void addParameterTypeToAllMethods(final String parameterType) {
        for (MethodModel m : methods) {
            m.addParameterType(parameterType);
        }
    }

    public void setReturnTypeForAllMethods(final String returnType) {
        for (MethodModel m : methods) {
            m.setReturnType(returnType);
        }
    }

    public Set<String> getReturnTypes() {
        Set<String> result = new HashSet<>();
        for (MethodModel methodModel : methods) {
            result.add(methodModel.getReturnType());
        }
        return result;
    }

    public List<MethodModel> selectMethods(final Set<String> names) {
        List<MethodModel> result = new ArrayList<>();
        for (MethodModel method : methods) {
            if(names.contains(method.getName())) {
                result.add(method);
            }
        }
        return result;
    }

    public void setOverridden(final Set<String> names) {
        for (MethodModel method : methods) {
            if (names.contains(method.getName())) {
                method.setOverridden(true);
            }
        }
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(final String superClass) {
        this.superClass = superClass;
    }

    @Override
    public String toString() {
        return "ClassModel of " + getFullName();
    }
}
