package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class EvilArguments {

  @Option(names = "--fancy")
  abstract protected String fancy();

  @Option(names = "--fAncy")
  abstract String fAncy();

  @Option(names = "--f_ancy")
  abstract String f_ancy();

  @Option(names = "--f__ancy")
  abstract String f__ancy();

  @Option(names = "--blub")
  abstract String blub();

  @Option(names = "--Blub")
  abstract String Blub();
}
