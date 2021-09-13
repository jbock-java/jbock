package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameters;

import java.util.List;

/**
 * No named options, this should generate the smallest possible parser.
 */
@Command(skipGeneratingParseOrExitMethod = true)
abstract class ExtremelySimpleArguments {

    @Parameters
    abstract List<String> hello();
}
