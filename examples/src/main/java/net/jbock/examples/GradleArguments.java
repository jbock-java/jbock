package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@Command
abstract class GradleArguments {

  /**
   * the message
   * message goes here
   */
  @Option(
      value = "message",
      mnemonic = 'm')
  abstract Optional<String> message();

  /**
   * the files
   */
  @Option(
      value = "file",
      mnemonic = 'f')
  abstract List<String> file();

  /**
   * the dir
   */
  @Option(value = "dir")
  abstract Optional<String> dir();

  /**
   * cmos flag
   */
  @Option(value = "c", mnemonic = 'c')
  abstract Boolean cmos();

  @Option(
      value = "verbose",
      mnemonic = 'v')
  abstract boolean verbose();

  @Param(value = 1)
  abstract List<String> otherTokens();

  @Command
  static abstract class Foo {

    @Option(value = "bar")
    abstract Optional<Integer> bar();
  }

  @Command
  static abstract class Bar {

    @Option(value = "bar")
    abstract List<String> bar();
  }
}
