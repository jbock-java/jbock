package net.jbock.examples;

import net.jbock.CommandLineArguments;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllCharactersArguments {

  abstract char smallChar();

  abstract Character bigChar();

  abstract Optional<Character> charOpt();

  abstract List<Character> charList();
}
