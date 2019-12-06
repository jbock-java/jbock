package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
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
