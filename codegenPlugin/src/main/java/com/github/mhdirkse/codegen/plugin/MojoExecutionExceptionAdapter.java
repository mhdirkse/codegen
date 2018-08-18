package com.github.mhdirkse.codegen.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

class MojoExecutionExceptionAdapter<T extends Exception> {
    @FunctionalInterface
    interface Wrapped<T extends Exception> {
        void run() throws MojoExecutionException, T;
    }

    private final String activityMsg;
    private final Wrapped<T> wrapped;

    MojoExecutionExceptionAdapter(
            final String activityMsg,
            final Wrapped<T> wrapped) {
        this.activityMsg = activityMsg;
        this.wrapped = wrapped;
    }

    void run() throws MojoExecutionException {
        try {
            wrapped.run();
        }
        catch(Exception e) {
            throw new MojoExecutionException(
                String.format("Unknown error while: %s", activityMsg), e);
        }
    }

    private static class ItemListImpl implements ItemList {
        private List<MojoExecutionExceptionAdapter<? extends Exception>> list =
                new ArrayList<>();

        @Override
        public <T extends Exception> ItemList add(
                final String activityMsg, final Wrapped<T> wrapped) {
            MojoExecutionExceptionAdapter<T> item =
                    new MojoExecutionExceptionAdapter<>(activityMsg, wrapped);
            list.add(item);
            return this;
        }

        @Override
        public void run() throws MojoExecutionException {
            for(MojoExecutionExceptionAdapter<? extends Exception> a : list) {
                a.run();
            }
        }
    }

    static interface ItemList {
        <T extends Exception> ItemList add(String activityMsg, final Wrapped<T> wrapped);
        void run() throws MojoExecutionException;
    }

    static ItemList list() {
        return new ItemListImpl();
    }
}
