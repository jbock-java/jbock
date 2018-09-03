package net.jbock.examples;

import java.util.OptionalInt;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;

// Allow positional arguments to start with a dash, so negative numbers can be passed.
@CommandLineArguments(strict = false)
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
