package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;
import net.jbock.util.Either;

@Command
interface MyBooleanCommand {

    @Parameter(converter = "parseBoolean")
    Boolean isBoolean();

    static Either<String, Boolean> parseBoolean(String token) {
        return Either.right(Boolean.valueOf(token));
    }
}
