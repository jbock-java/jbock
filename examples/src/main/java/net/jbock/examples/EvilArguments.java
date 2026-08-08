package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.Optional;

@Command
interface EvilArguments {

    @Option(names = "--Fancy")
    Optional<String> Fancy();

    @Option(names = "--fancy")
    String fancy();

    @Option(names = "--fAncy")
    String fAncy();

    @Option(names = "--f_ancy")
    String f_ancy();

    @Option(names = "--f__ancy")
    String f__ancy();

    @Option(names = "--blub")
    String blub();

    @Option(names = "--Blub")
    String Blub();

    @Option(names = "--evil")
    Optional<String> __();
}
