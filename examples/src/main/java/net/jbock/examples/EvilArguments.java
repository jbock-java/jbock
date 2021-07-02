package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.either.Optional;

@Command
abstract class EvilArguments {

  @Option(names = "--Fancy")
  abstract Optional<String> Fancy();

  @Option(names = "--fancy")
  abstract String fancy();

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

  @Option(names = "--evil")
  abstract Optional<String> __();
}
