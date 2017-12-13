package net.jbock.examples;

import java.util.OptionalInt;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments(ignoreDashes = true)
abstract class AdditionArguments {

  @Positional
  abstract int a();

  @Positional
  abstract int b();

  @Positional
  abstract OptionalInt c();

  final int sum() {
    return a() + b() + c().orElse(0);
  }
}
