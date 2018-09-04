package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.ShortName;

import java.util.OptionalInt;

@CommandLineArguments
abstract class PsArguments {

  @ShortName('a')
  abstract boolean all();

  @ShortName('w')
  @Description(argumentName = "number")
  abstract OptionalInt wide();
}
