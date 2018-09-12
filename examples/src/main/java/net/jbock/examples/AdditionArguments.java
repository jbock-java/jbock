package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.OptionalInt;

// Allow positional arguments to start with a dash, so negative numbers can be passed.
@CommandLineArguments(strict = false)
public abstract class AdditionArguments {

  /**
   * First argument
   */
  @PositionalParameter
  abstract int a();

  /**
   * Second argument
   */
  @PositionalParameter
  abstract int b();

  /**
   * Optional third argument
   */
  @PositionalParameter
  abstract OptionalInt c();

  final int sum() {
    return a() + b() + c().orElse(0);
  }
}
