package com.github.mhdirkse.codegen.plugin.impl;

import org.apache.velocity.VelocityContext;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
class FileContentsDefinition {
    private VelocityContext velocityContext;
    private String templateFileName;
    private String outputClassName;
}