package com.github.mhdirkse.codegen.plugin;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class CodegenMojoTest {
    @Test
    public void testPackageToRelativePathLinux() {
        Assert.assertEquals(adjustForOS("base/com/github/mhdirkse/X.java"), CodegenMojo.classToPathOfJavaFile(
                new File("base"), "com.github.mhdirkse.X").toString());
    }

    private static String adjustForOS(String linuxPath) {
        String replacement = "\\\\";
        if(File.separator.equals("/")) {
            replacement = "/";
        }
        return linuxPath.replaceAll("/", replacement);
    }

    @Test
    public void testPackageToRelativePathLinuxNoPath() {
        Assert.assertEquals(adjustForOS("base/X.java"), CodegenMojo.classToPathOfJavaFile(
                new File("base"), "X").toString());
    }
}
