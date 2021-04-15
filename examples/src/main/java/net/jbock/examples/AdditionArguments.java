package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

import java.util.Optional;

// Allow positional arguments to start with a dash, so negative numbers can be passed.
@Command
abstract class AdditionArguments {

  /**
   * First argument
   */
  @Param(0)
  abstract int a();

  /**
   * Second argument
   */
  @Param(1)
  abstract int b();

  /**
   * Optional third argument
   */
  @Param(2)
  abstract Optional<Integer> c();

  final int sum() {
    return a() + b() + c().orElse(0);
  }
}
