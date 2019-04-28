package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.Optional;

@CommandLineArguments
abstract class SimpleArguments {

  @Parameter(shortName = 'x')
  abstract boolean extract();

  @Parameter(longName = "file")
  abstract Optional<String> file();
}
