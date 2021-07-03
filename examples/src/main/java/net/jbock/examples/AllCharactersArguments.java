package net.jbock.examples;

import io.jbock.util.Optional;
import net.jbock.Command;
import net.jbock.Option;

import java.util.List;

@Command
abstract class AllCharactersArguments {

    @Option(names = "--smallChar")
    abstract char smallChar();

    @Option(names = "--bigChar")
    abstract Character bigChar();

    @Option(names = "--charOpt")
    abstract Optional<Character> charOpt();

    @Option(names = "--charList")
    abstract List<Character> charList();
}
