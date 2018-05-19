package com.github.mhdirkse.refplug;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class MyMojoTest {
    @Test
    public void testPackageToRelativePathLinux() {
        Assert.assertEquals("base/com/github/mhdirkse/X.java", MyMojo.classToPathOfJavaFile(
                new File("base"), "com.github.mhdirkse.X").toString());
    }

    @Test
    public void testPackageToRelativePathLinuxNoPath() {
        Assert.assertEquals("base/X.java", MyMojo.classToPathOfJavaFile(
                new File("base"), "X").toString());
    }
}
