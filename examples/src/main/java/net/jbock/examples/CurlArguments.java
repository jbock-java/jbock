package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command(name = "curl",
        publicParser = true,
        description = {
                "curl  is  a  tool  to  transfer data from or to a server",
                "using one of the supported protocols.",
                "curl offers a busload of useful tricks.",
                "curl is powered by libcurl for all transfer-related features.",
                "See libcurl(3) for details."})
interface CurlArguments {

    @Option(names = {"--request", "-X"},
            description = "Optional<String> for regular arguments")
    Optional<String> method();

    @Option(names = {"--header", "-H"},
            description = "List<String> for repeatable arguments")
    List<String> headers();

    @Option(names = {"--verbose", "-v"},
            description = "boolean for flags")
    boolean verbose();

    @Option(names = {"--include", "-i"})
    boolean include();

    List<String> url();
}
