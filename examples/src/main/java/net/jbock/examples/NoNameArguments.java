package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class NoNameArguments {

  @Parameter(value = "message")
  abstract Optional<String> message();

  @Parameter(value = "file")
  abstract List<String> file();

  @Parameter(mnemonic = 'v', value = "verbosity")
  abstract Optional<Integer> verbosity();

  @Parameter(mnemonic = 'n', value = "number")
  abstract int number();

  @Parameter(value = "cmos")
  abstract boolean cmos();
}
