package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class EvilArguments {

  @Parameter(longName = "fancy")
  abstract protected String fancy();

  @Parameter(longName = "fAncy")
  abstract String fAncy();

  @Parameter(longName = "f_ancy")
  abstract String f_ancy();

  @Parameter(longName = "f__ancy")
  abstract String f__ancy();

  @Parameter(longName = "blub")
  abstract String blub();

  @Parameter(longName = "Blub")
  abstract String Blub();
}
