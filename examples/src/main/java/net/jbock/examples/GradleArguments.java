package net.jbock.examples;


import java.util.List;
import java.util.Optional;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.Positional;
import net.jbock.ShortName;
import net.jbock.SuppressLongName;

@CommandLineArguments(allowGrouping = true)
abstract class GradleArguments {

  @ShortName('m')
  @Description(value = {"the message", "message goes here"}, argumentName = "MESSAGE")
  abstract Optional<String> message();

  @ShortName('f')
  @Description(value = "the files", argumentName = "FILE")
  abstract List<String> file();

  @Description(value = "the dir", argumentName = "DIR")
  abstract Optional<String> dir();

  @ShortName('c')
  @SuppressLongName
  @Description("cmos flag")
  abstract boolean cmos();

  @ShortName('v')
  abstract boolean verbose();

  @Positional
  abstract List<String> otherTokens();

  @Positional
  abstract List<String> ddTokens();

  @CommandLineArguments
  static abstract class Foo {
    abstract Optional<String> bar();
  }

  @CommandLineArguments
  static abstract class Bar {
    abstract List<String> bar();
  }

  @CommandLineArguments(allowGrouping = true)
  static abstract class GroupingFoo {
    abstract Optional<String> bar();
  }

  @CommandLineArguments(allowGrouping = true)
  static abstract class GroupingBar {
    abstract List<String> bar();
  }
}
