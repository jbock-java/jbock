package net.jbock.examples;

import java.util.List;
import java.util.Optional;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;
import net.jbock.SuppressLongName;

@CommandLineArguments(grouping = true)
abstract class CurlArguments {

  @ShortName('X')
  @LongName("request")
  @Description("Optional<String> for regular arguments")
  abstract Optional<String> method();

  @ShortName('H')
  @SuppressLongName
  @Description("List<String> for repeatable arguments")
  abstract List<String> headers();

  @ShortName('v')
  @SuppressLongName
  @Description("boolean for flags")
  abstract boolean verbose();

  @ShortName('i')
  abstract boolean include();

  @Positional
  @Description({
      "@OtherTokens to capture any 'other' tokens in the input.",
      "In this case, that's any token which doesn't match one of",
      "/-v/, /-X(=.*)?/, /--request(=.*)?/, or /-H(=.*)?/,",
      "or follows immediately after the equality-less version",
      "of one of the latter 3.",
      "If there were no method with the @OtherTokens annotation,",
      "such a token would cause an IllegalArgumentException to be",
      "thrown from the CurlArguments_Parser.parse method."})
  abstract List<String> urls();
}
