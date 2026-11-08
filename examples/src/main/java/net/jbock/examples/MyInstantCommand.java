package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;
import net.jbock.util.Either;

import java.time.Instant;

@Command
interface MyInstantCommand {

    @Parameter(converter = "parseInstant")
    Instant instant();

    static Either<String, Instant> parseInstant(String token) {
        try {
            return Either.right(Instant.parse(token));
        } catch (Exception e) {
            return Either.left(e.getMessage());
        }
    }
}
