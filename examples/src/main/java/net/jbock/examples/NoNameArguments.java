package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.ShortName;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

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
