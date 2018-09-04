package net.jbock.examples;

import java.util.OptionalInt;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.Positional;

// Allow positional arguments to start with a dash, so negative numbers can be passed.
@CommandLineArguments(strict = false)
abstract class AdditionArguments {

  @Positional
  @Description("First argument")
  abstract int a();

  @Positional
  @Description("Second argument")
  abstract int b();

  @Positional
  @Description("Optional third argument")
  abstract OptionalInt c();

  final int sum() {
    return a() + b() + c().orElse(0);
  }
}
