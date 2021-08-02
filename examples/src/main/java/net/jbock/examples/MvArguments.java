package net.jbock.examples;

import net.jbock.Command;

@Command
abstract class MvArguments implements MvArguments_Parent, MvArguments_ParentParent {

    @Override
    public abstract String dest();

    @Override
    public boolean isSafe() {
        return true;
    }
}
