package net.jbock.examples;

import net.jbock.Command;
import net.jbock.VarargsParameter;

import java.util.List;

/**
 * No named options, this should generate the smallest possible parser.
 */
@Command(skipGeneratingParseOrExitMethod = true)
abstract class ExtremelySimpleArguments {

    @VarargsParameter
    abstract List<String> hello();
}
