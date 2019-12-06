package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class EvilArguments {

  @Option(value = "fancy")
  abstract protected String fancy();

  @Option(value = "fAncy")
  abstract String fAncy();

  @Option(value = "f_ancy")
  abstract String f_ancy();

  @Option(value = "f__ancy")
  abstract String f__ancy();

  @Option(value = "blub")
  abstract String blub();

  @Option(value = "Blub")
  abstract String Blub();
}
