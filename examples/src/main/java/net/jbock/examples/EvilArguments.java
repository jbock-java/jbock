package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class EvilArguments {

  @Option("fancy")
  abstract protected String fancy();

  @Option("fAncy")
  abstract String fAncy();

  @Option("f_ancy")
  abstract String f_ancy();

  @Option("f__ancy")
  abstract String f__ancy();

  @Option("blub")
  abstract String blub();

  @Option("Blub")
  abstract String Blub();
}
