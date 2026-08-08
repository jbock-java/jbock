package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.VarargsParameter;

import java.util.List;

@Command(description = "ouch", descriptionKey = "description.main")
interface RestArguments {

    @Option(names = "--file",
            description = "This is the file.",
            descriptionKey = "the.file")
    List<String> file();

    @VarargsParameter(descriptionKey = "the.rest")
    List<String> rest();
}
