package com.github.mhdirkse.refplug;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class VelocityContextMethod {
    private String name;
    private String signature;

    public VelocityContextMethod(final Method reflectionMethod) {
        name = reflectionMethod.getName();
        List<String> parameters = new ArrayList<>();
        int seq = 0;
        for (Class<?> reflectionParameterType : reflectionMethod.getParameterTypes()) {
            parameters.add(
                    String.format("%s p%d",
                            reflectionParameterType.getCanonicalName(),
                            ++seq));
        }
        signature = StringUtils.join(parameters, ", ");
    }

    public final String getName() {
        return name;
    }
    public final String getSignature() {
        return signature;
    }
}
