package net.jbock.examples;


import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.jbock.CommandLineArguments;
import net.jbock.ShortName;

@CommandLineArguments
abstract class NoNameArguments {

  abstract Optional<String> message();

  abstract List<String> file();

  @ShortName('v')
  abstract OptionalInt verbosity();

  @ShortName('n')
  abstract int number();

  abstract boolean cmos();
}
