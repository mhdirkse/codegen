package com.github.mhdirkse.codegen.plugin;

public final class Utils {
    private Utils() {
    }

    public static String getErrorMessage(final int line, final int column, final String text) {
        return String.format("Line %d column %d: %s", line, column, text);
    }
}
