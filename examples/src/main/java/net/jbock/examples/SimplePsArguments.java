package net.jbock.examples;

import java.util.OptionalInt;
import net.jbock.CommandLineArguments;
import net.jbock.ShortName;

@CommandLineArguments
abstract class SimplePsArguments {

  @ShortName('a')
  abstract boolean all();

  @ShortName('w')
  abstract OptionalInt wide();

}
