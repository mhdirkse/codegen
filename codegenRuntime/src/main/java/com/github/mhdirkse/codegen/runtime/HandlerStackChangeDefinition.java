package com.github.mhdirkse.codegen.runtime;

abstract class HandlerStackChangeDefinition<H> {
    static interface Visitor<H> {
        void addFirstCmdExec(int atSeq, H handler);
        void removeFirstCmdExec(int atSeq);
        void removeAllPreceedingCmdExec(int atSeq);
    }

    abstract void accept(Visitor<H> v);

    static class AddFirst<H> extends HandlerStackChangeDefinition<H> {
        private final int atSeq;
        private final H handler;

        AddFirst(final int atSeq, final H handler) {
            this.atSeq = atSeq;
            this.handler = handler;
        }

        @Override
        void accept(final Visitor<H> v) {
            v.addFirstCmdExec(atSeq, handler);
        }
    }

    static class RemoveFirst<H> extends HandlerStackChangeDefinition<H> {
        private final int atSeq;
 
        RemoveFirst(final int atSeq) {
            this.atSeq = atSeq;
        }

        @Override
        void accept(final Visitor<H> v) {
            v.removeFirstCmdExec(atSeq);
        }
    }

    static class RemoveAllPreceeding<H> extends HandlerStackChangeDefinition<H> {
        private final int atSeq;
        
        RemoveAllPreceeding(final int atSeq) {
            this.atSeq = atSeq;
        }

        @Override
        void accept(final Visitor<H> v) {
            v.removeAllPreceedingCmdExec(atSeq);
        }
    }
}
