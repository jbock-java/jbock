package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
interface AllCharactersArguments {

    @Option(names = "--smallChar")
    char smallChar();

    @Option(names = "--bigChar")
    Character bigChar();

    @Option(names = "--charOpt")
    Optional<Character> charOpt();

    @Option(names = "--charList")
    List<Character> charList();
}
