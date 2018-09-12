package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class EvilArguments {

  @Parameter
  abstract protected String fancy();

  @Parameter
  abstract String fAncy();

  @Parameter
  abstract String f_ancy();

  @Parameter
  abstract String blub();

  @Parameter
  abstract String Blub();
}
