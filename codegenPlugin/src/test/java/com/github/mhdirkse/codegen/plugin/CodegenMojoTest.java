package com.github.mhdirkse.codegen.plugin;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.github.mhdirkse.codegen.plugin.CodegenMojo;

public class CodegenMojoTest {
    @Test
    public void testPackageToRelativePathLinux() {
        Assert.assertEquals("base/com/github/mhdirkse/X.java", CodegenMojo.classToPathOfJavaFile(
                new File("base"), "com.github.mhdirkse.X").toString());
    }

    @Test
    public void testPackageToRelativePathLinuxNoPath() {
        Assert.assertEquals("base/X.java", CodegenMojo.classToPathOfJavaFile(
                new File("base"), "X").toString());
    }
}
