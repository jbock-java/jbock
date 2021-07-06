package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;

@Command(description = "ouch", descriptionKey = "description.main")
abstract class RestArguments {

    @Option(names = "--file",
            description = "This is the file.",
            descriptionKey = "the.file")
    abstract List<String> file();

    @Parameters(descriptionKey = "the.rest")
    abstract List<String> rest();
}
