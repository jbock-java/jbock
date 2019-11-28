package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class EvilArguments {

  @Parameter(value = "fancy")
  abstract protected String fancy();

  @Parameter(value = "fAncy")
  abstract String fAncy();

  @Parameter(value = "f_ancy")
  abstract String f_ancy();

  @Parameter(value = "f__ancy")
  abstract String f__ancy();

  @Parameter(value = "blub")
  abstract String blub();

  @Parameter(value = "Blub")
  abstract String Blub();
}
