package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

import java.util.Optional;

@Command
abstract class AdditionArguments {

  /**
   * First argument
   */
  @Parameter(index = 0)
  abstract int a();

  /**
   * Second argument
   */
  @Parameter(index = 1)
  abstract int b();

  /**
   * Optional third argument
   */
  @Parameter(index = 2)
  abstract Optional<Integer> c();

  final int sum() {
    return a() + b() + c().orElse(0);
  }
}
