package net.jbock.examples;

import io.jbock.util.Optional;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;

/**
 * curl  is  a  tool  to  transfer data from or to a server
 * using one of the supported protocols.
 * curl offers a busload of useful tricks.
 * curl is powered by libcurl for all transfer-related features.
 * See libcurl(3) for details.
 */
@Command(name = "curl")
abstract class CurlArguments {

    /**
     * Optional<String> for regular arguments
     */
    @Option(names = {"--request", "-X"})
    abstract Optional<String> method();

    /**
     * List<String> for repeatable arguments
     */
    @Option(names = {"--header", "-H"})
    abstract List<String> headers();

    /**
     * boolean for flags
     */
    @Option(names = {"--verbose", "-v"})
    abstract boolean verbose();

    @Option(names = {"--include", "-i"})
    abstract boolean include();

    @Parameters
    abstract List<String> url();
}
