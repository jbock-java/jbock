package net.zerobuilder.examples.gradle;

import java.util.List;
import java.util.Optional;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

@CommandLineArguments
abstract class CurlArguments {

  @ShortName('X')
  @LongName("method")
  @Description("Optional<String> for regular arguments")
  abstract Optional<String> method();

  @ShortName('H')
  @LongName("header")
  @Description("List<String> for repeatable arguments")
  abstract List<String> headers();

  @ShortName('v')
  @Description("boolean for flags")
  abstract boolean verbose();

  @OtherTokens
  @Description({
      "@OtherTokens to capture any 'other' tokens in the input.",
      "In this case, that's any token which is not one of",
      "'-v', '--verbose', '-X', '--method', '-H', '--header',",
      "or follows immediately after one of the latter 4.",
      "If there were no method with the @OtherTokens annotation,",
      "such a token would cause an IllegalArgumentException to be",
      "thrown from the CurlArguments_Parser.parse method."})
  abstract List<String> urls();
}
