package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.Optional;

@CommandLineArguments
abstract class SimpleArguments {

  @Parameter(value = "x", mnemonic = 'x')
  abstract boolean extract();

  @Parameter("file")
  abstract Optional<String> file();
}
