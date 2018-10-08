package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllCharactersArguments {

  @Parameter
  abstract char smallChar();

  @Parameter
  abstract Character bigChar();

  @Parameter(optional = true)
  abstract Optional<Character> charOpt();

  @Parameter(repeatable = true)
  abstract List<Character> charList();
}
