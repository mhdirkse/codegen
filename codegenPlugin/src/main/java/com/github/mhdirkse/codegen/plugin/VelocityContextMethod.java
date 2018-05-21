package com.github.mhdirkse.codegen.plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class VelocityContextMethod {
    private String name;
    private String signature;
    private String signatureForHandler;
    private String finalSignature;
    private String finalSignatureForHandler;
    private String actualParameters;
    private String actualParametersForHandler;

    public VelocityContextMethod(final Method reflectionMethod, final String handlerStackTypeParameter) {
        name = reflectionMethod.getName();
        List<String> parameters = new ArrayList<>();
        List<String> finalParameters = new ArrayList<>();
        int seq = 0;
        for (Class<?> reflectionParameterType : reflectionMethod.getParameterTypes()) {
            String simpleParameterSignature = String.format("%s p%d",
                    reflectionParameterType.getCanonicalName(), ++seq);
            parameters.add(simpleParameterSignature);
            finalParameters.add("final " + simpleParameterSignature);
        }
        signature = StringUtils.join(parameters, ", ");
        finalSignature = StringUtils.join(finalParameters, ", ");
        String simpleContextParameterSignature =
                String.format("com.github.mhdirkse.codegen.runtime.HandlerStackContext<%s> ctx",
                        handlerStackTypeParameter);
        parameters.add(simpleContextParameterSignature);
        finalParameters.add("final " + simpleContextParameterSignature);
        signatureForHandler = StringUtils.join(parameters, ",");
        finalSignatureForHandler = StringUtils.join(finalParameters, ", ");

        List<String> actualParametersList = new ArrayList<>();
        for(int i = 1; i <= seq; ++i) {
            actualParametersList.add("p" + new Integer(i).toString());
        }
        actualParameters = StringUtils.join(actualParametersList, ", ");
        actualParametersList.add("ctx");
        actualParametersForHandler = StringUtils.join(actualParametersList, ", ");
    }

    public final String getName() {
        return name;
    }

    public final String getSignature() {
        return signature;
    }

    public final String getSignatureForHandler() {
        return signatureForHandler;
    }

    public final String getFinalSignature() {
        return finalSignature;
    }

    public final String getFinalSignatureForHandler() {
        return finalSignatureForHandler;
    }

    public final String getActualParameters() {
        return actualParameters;
    }

    public final String getActualParametersForHandler() {
        return actualParametersForHandler;
    }
}
