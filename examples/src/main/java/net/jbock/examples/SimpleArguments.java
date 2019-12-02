package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;

import java.util.Optional;

@CLI
abstract class SimpleArguments {

  @Option(value = "x", mnemonic = 'x')
  abstract boolean extract();

  @Option("file")
  abstract Optional<String> file();
}
