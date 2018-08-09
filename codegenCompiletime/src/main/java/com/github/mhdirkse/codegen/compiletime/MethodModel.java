package com.github.mhdirkse.codegen.compiletime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MethodModel {
    private String name;
    private String returnType;
    private List<String> parameterTypes;
    private boolean overridden = false;

    public MethodModel() {
    }

    public MethodModel(final MethodModel other) {
        this.name = other.name;
        this.returnType = other.returnType;
        this.parameterTypes = copyParameterTypes(other.getParameterTypes());
        this.overridden = other.overridden;
    }

    private static List<String> copyParameterTypes(final List<String> source) {
        List<String> result = null;
        if (source != null) {
            result = new ArrayList<>();
            result.addAll(source);
        }
        return result;
    }

    public MethodModel(final Method m) {
        name = m.getName();
        returnType = m.getReturnType().getCanonicalName();
        parameterTypes = new ArrayList<>();
        for (Class<?> reflectionParameterType : m.getParameterTypes()) {
            parameterTypes.add(reflectionParameterType.getCanonicalName());
        }
        overridden = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParameterTypes() {
        return copyParameterTypes(parameterTypes);
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = copyParameterTypes(parameterTypes);
    }

    public String getFormalParametersInterface() {
        return getFormalParameters("");
    }

    public String getFormalParametersClass() {
        return getFormalParameters("final ");
    }

    private String getFormalParameters(final String finalOrEmpty) {
        List<String> formalParameters = new ArrayList<>();
        int seq = 0;
        for (String parameterType : parameterTypes) {
            formalParameters.add(String.format("%s%s p%d",
                    finalOrEmpty, parameterType, ++seq));
        }
        return StringUtils.join(formalParameters, ", ");
    }

    public String getActualParameters() {
        List<String> actualParameters = getActualParametersList();
        return StringUtils.join(actualParameters, ", ");
    }

    private List<String> getActualParametersList() {
        List<String> actualParameters = new ArrayList<>();
        for (int i = 0; i < parameterTypes.size(); ++i) {
            actualParameters.add(String.format("p%d", i+1));
        }
        return actualParameters;
    }

    public String getActualParametersWith(final String extra) {
        List<String> result = getActualParametersList();
        result.add(extra);
        return StringUtils.join(result, ", ");
    }

    public void addParameterType(final String parameterType) {
        parameterTypes.add(parameterType);
    }

    boolean isParameterTypesSame(final List<String> other) {
        return parameterTypes == other;
    }

    boolean sharesParameterTypesRef(final MethodModel other) {
        return parameterTypes == other.parameterTypes;
    }

    public boolean getOverridden() {
        return overridden;
    }

    public void setOverridden(final boolean overridden) {
        this.overridden = overridden;
    }
}
