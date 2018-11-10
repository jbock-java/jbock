package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.Optional;

// Allow positional arguments to start with a dash, so negative numbers can be passed.
@CommandLineArguments(allowPrefixedTokens = true)
public abstract class AdditionArguments {

  /**
   * First argument
   */
  @PositionalParameter
  abstract int a();

  /**
   * Second argument
   */
  @PositionalParameter(position = 1)
  abstract int b();

  /**
   * Optional third argument
   */
  @PositionalParameter(optional = true)
  abstract Optional<Integer> c();

  final int sum() {
    return a() + b() + c().orElse(0);
  }
}
