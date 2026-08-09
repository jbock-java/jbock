package net.jbock.examples;

import net.jbock.Command;

import java.util.List;

/**
 * No named options, this should generate the smallest possible parser.
 */
@Command(skipGeneratingParseOrExitMethod = true)
interface ExtremelySimpleArguments {

    List<String> hello();
}
