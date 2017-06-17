package net.zerobuilder.examples.gradle;

import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

import java.util.List;
import java.util.Optional;

final class Curl {

  final List<String> headers;
  final List<String> urls;
  final String method;
  final boolean verbose;

@CommandLineArguments
Curl(@ShortName('H') @Description(
    "List<String> for arguments that appear multiple times")
         List<String> headers,
     @ShortName('v') @Description(
         "boolean for flags")
         boolean verbose,
     @ShortName('X') @Description(
         "String or Optional<String> for regular arguments")
         Optional<String> method,
     @OtherTokens @Description({
         "@OtherTokens to capture everything else.",
         "In this case, everything that isn't '-v' or follows '-H' or '-X'"})
         List<String> urls) {
  this.headers = headers;
  this.verbose = verbose;
  this.method = method.orElse("GET");
  this.urls = urls;
}
}
