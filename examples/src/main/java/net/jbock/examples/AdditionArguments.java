package net.jbock.examples;

import java.util.OptionalInt;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments
abstract class AdditionArguments {

  @Positional
  abstract int a();

  @Positional
  abstract int b();

  @Positional
  abstract OptionalInt c();
}
