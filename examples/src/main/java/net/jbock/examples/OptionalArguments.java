package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
public abstract class OptionalArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Parameter(shortName = 'a', mappedBy = AlphaMapper.class)
  abstract Optional<Integer> a();

//  @Parameter(shortName = 'b', mappedBy = BetaMapper.class)
//  abstract OptionalInt b();

  static class AlphaMapper implements Supplier<Function<String, Optional<Integer>>> {

    @Override
    public Function<String, Optional<Integer>> get() {
      return PARSE_INT.andThen(Optional::of);
    }
  }

  static class BetaMapper implements Supplier<Function<String, OptionalInt>> {

    @Override
    public Function<String, OptionalInt> get() {
      return PARSE_INT.andThen(OptionalInt::of);
    }
  }
}
