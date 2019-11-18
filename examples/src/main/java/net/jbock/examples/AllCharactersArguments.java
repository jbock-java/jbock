package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllCharactersArguments {

  @Parameter(value = "smallChar")
  abstract char smallChar();

  @Parameter(value = "bigChar")
  abstract Character bigChar();

  @Parameter(value = "charOpt")
  abstract Optional<Character> charOpt();

  @Parameter(value = "charList")
  abstract List<Character> charList();
}
