package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameters;

import java.util.List;

@Command(generateParseOrExitMethod = false)
abstract class ExtremelySimpleArguments {

    @Parameters
    abstract List<String> hello();
}
