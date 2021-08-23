package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

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
abstract class CurlArguments {

    @Option(names = {"--request", "-X"},
            description = "Optional<String> for regular arguments")
    abstract Optional<String> method();

    @Option(names = {"--header", "-H"},
            description = "List<String> for repeatable arguments")
    abstract List<String> headers();

    @Option(names = {"--verbose", "-v"},
            description = "boolean for flags")
    abstract boolean verbose();

    @Option(names = {"--include", "-i"})
    abstract boolean include();

    @Parameters
    abstract List<String> url();
}
