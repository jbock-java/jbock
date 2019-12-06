package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
abstract class AllCharactersArguments {

  @Option(value = "smallChar")
  abstract char smallChar();

  @Option(value = "bigChar")
  abstract Character bigChar();

  @Option(value = "charOpt")
  abstract Optional<Character> charOpt();

  @Option(value = "charList")
  abstract List<Character> charList();
}
