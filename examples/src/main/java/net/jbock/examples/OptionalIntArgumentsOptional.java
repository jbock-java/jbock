package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.OptionalInt;
import java.util.function.Supplier;

@Command
interface OptionalIntArgumentsOptional {

    @Option(names = {"--a", "-a"}, converter = MyConverter.class)
    OptionalInt a();

    class MyConverter implements Supplier<StringConverter<Integer>> {

        @Override
        public StringConverter<Integer> get() {
            return StringConverter.create(Integer::parseInt);
        }
    }
}
