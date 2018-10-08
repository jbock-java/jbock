package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@CommandLineArguments
abstract class NoNameArguments {

  @Parameter(optional = true)
  abstract Optional<String> message();

  @Parameter(repeatable = true)
  abstract List<String> file();

  @Parameter(optional = true, shortName = 'v')
  abstract OptionalInt verbosity();

  @Parameter(shortName = 'n')
  abstract int number();

  @Parameter(flag = true)
  abstract boolean cmos();
}
