package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class NoNameArguments {

  @Parameter(longName = "message", optional = true)
  abstract Optional<String> message();

  @Parameter(longName = "file", repeatable = true)
  abstract List<String> file();

  @Parameter(optional = true, shortName = 'v', longName = "verbosity")
  abstract Optional<Integer> verbosity();

  @Parameter(shortName = 'n', longName = "number")
  abstract int number();

  @Parameter(longName = "cmos", flag = true)
  abstract boolean cmos();
}
