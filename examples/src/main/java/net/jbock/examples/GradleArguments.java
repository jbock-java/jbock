package net.jbock.examples;


import net.jbock.*;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@CommandLineArguments(allowEscape = true)
abstract class GradleArguments {

  @ShortName('m')
  @Description(value = {"the message", "message goes here"})
  abstract Optional<String> message();

  @ShortName('f')
  @Description(value = "the files")
  abstract List<String> file();

  @Description(value = "the dir")
  abstract Optional<String> dir();

  @ShortName('c')
  @LongName("")
  @Description("cmos flag")
  abstract boolean cmos();

  @ShortName('v')
  abstract boolean verbose();

  @Positional
  abstract List<String> otherTokens();

  @CommandLineArguments
  static abstract class Foo {
    abstract OptionalInt bar();
  }

  @CommandLineArguments
  static abstract class Bar {
    abstract List<String> bar();
  }
}
