package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllCharactersArguments {

  @Parameter(longName = "smallChar")
  abstract char smallChar();

  @Parameter(longName = "bigChar")
  abstract Character bigChar();

  @Parameter(longName = "charOpt")
  abstract Optional<Character> charOpt();

  @Parameter(longName = "charList")
  abstract List<Character> charList();
}
