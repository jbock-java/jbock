package net.jbock.examples;


import net.jbock.CLI;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@CLI
abstract class NoNameArguments {

  @Option(value = "message")
  abstract Optional<String> message();

  @Option(value = "file")
  abstract List<String> file();

  @Option(mnemonic = 'v', value = "verbosity")
  abstract Optional<Integer> verbosity();

  @Option(mnemonic = 'n', value = "number")
  abstract int number();

  @Option(value = "cmos")
  abstract boolean cmos();
}
