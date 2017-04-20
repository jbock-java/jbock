package net.zerobuilder.examples.gradle;

import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

import java.util.List;

final class Curl {

  final List<String> headers;
  final List<String> urls;
  final String method;
  final boolean verbose;

  @CommandLineArguments
  Curl(@ShortName('H') @Description("List<String> for arguments that appear multiple times")
           List<String> headers,
       @ShortName('v') @Description("boolean for flags")
           boolean verbose,
       @ShortName('X') @Description("String for regular arguments")
           String method,
       @OtherTokens @Description("Everything that isn't '-v' or follows '-H' or '-X'")
           List<String> urls) {
    this.headers = headers;
    this.verbose = verbose;
    this.method = method == null ? "GET" : method;
    this.urls = urls;
  }
}
