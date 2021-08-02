package net.jbock.examples;

import net.jbock.Parameter;

interface MvArguments_ParentParent {

    @Parameter(index = 0)
    String source();

    @Parameter(index = 1)
    String dest();

    boolean isSafe();
}
