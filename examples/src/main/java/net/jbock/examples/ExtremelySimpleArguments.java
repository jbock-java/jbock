package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

import java.util.List;

/**
 * No named options, this should generate the smallest possible parser.
 */
@Command(skipGeneratingParseOrExitMethod = true)
interface ExtremelySimpleArguments {

    @Parameter
    List<String> hello();
}
