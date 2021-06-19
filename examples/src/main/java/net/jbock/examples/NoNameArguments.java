package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
abstract class NoNameArguments {

  @Option(names = "--message")
  abstract Optional<String> message();

  @Option(names = "--file")
  abstract List<String> file();

  @Option(names = {"--verbosity", "-v"})
  abstract Optional<Integer> verbosity();

  @Option(names = {"--number", "-n"})
  abstract int number();

  @Option(names = "--cmos")
  abstract boolean cmos();
}
