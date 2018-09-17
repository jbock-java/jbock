package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@CommandLineArguments
abstract class CustomMapperArguments {

  /**
   * The mapper must be a Function from String to whatever-this-returns.
   * It must also have a package-visible no-arg constructor.
   */
  @Parameter(mappedBy = DateMapper.class)
  abstract Date date();

  @Parameter(mappedBy = DateMapper.class)
  abstract Optional<Date> optDate();

  @Parameter(mappedBy = DateMapper.class)
  abstract List<Date> dateList();

  @Parameter(mappedBy = CustomBigIntegerMapper.class)
  abstract Optional<BigInteger> verbosity();

  static class DateMapper implements Function<String, Date> {

    @Override
    public Date apply(String s) {
      return new Date(Long.parseLong(s));
    }
  }
}
