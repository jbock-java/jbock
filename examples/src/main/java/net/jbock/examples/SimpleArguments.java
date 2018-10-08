package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.Optional;

@CommandLineArguments
abstract class SimpleArguments {

  @Parameter(flag = true, shortName = 'x')
  abstract boolean extract();

  @Parameter(optional = true)
  abstract Optional<String> file();
}
