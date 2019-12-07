package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
abstract class AllCharactersArguments {

  @Option("smallChar")
  abstract char smallChar();

  @Option("bigChar")
  abstract Character bigChar();

  @Option("charOpt")
  abstract Optional<Character> charOpt();

  @Option("charList")
  abstract List<Character> charList();
}
