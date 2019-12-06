package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.Optional;

@Command
abstract class SimpleArguments {

  @Option(value = "x", mnemonic = 'x')
  abstract boolean extract();

  @Option("file")
  abstract Optional<String> file();
}
