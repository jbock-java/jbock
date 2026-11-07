package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.util.Either;

import java.util.OptionalInt;

@Command
interface OptionalIntArgumentsOptional {

    @Option(names = {"--a", "-a"}, converter = "myConverter")
    OptionalInt a();

    static Either<String, Integer> myConverter(String token) {
        return Either.catching(Integer::parseInt, token).mapLeft(Exception::getMessage);
    }
}
