package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.OptionalInt;

/*
 * Option '-a' is not optional, because the output type of the converter
 * matches the option type exactly.
 */
@Command
abstract class OptionalIntArguments {

    @Option(names = {"--a", "-a"}, converter = MyConverter.class)
    abstract OptionalInt a();

    static class MyConverter extends StringConverter<OptionalInt> {

        @Override
        protected OptionalInt convert(String token) {
            return OptionalInt.of(Integer.parseInt(token));
        }
    }
}
