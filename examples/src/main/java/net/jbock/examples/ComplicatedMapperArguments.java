package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
interface ComplicatedMapperArguments {

    @Option(
            names = {"-N", "--number"},
            converter = MyConverter.class)
    Integer number();

    @Option(
            names = "--numbers",
            converter = LazyNumberConverter.class)
    List<LazyNumber> numbers();

    class LazyNumberConverter implements Supplier<StringConverter<LazyNumber>> {
        @Override
        public StringConverter<LazyNumber> get() {
            return StringConverter.create(s -> () -> Integer.valueOf(s));
        }
    }

    interface LazyNumber extends Supplier<Integer> {
    }

    class MyConverter implements Supplier<StringConverter<Integer>> {
        @Override
        public StringConverter<Integer> get() {
            return StringConverter.create(new Zapper());
        }
    }

    class Zapper implements Foo<String> {
        public Integer apply(String s) {
            return 1;
        }
    }

    interface Xi<A, T, B> extends Function<B, A> {
    }

    interface Zap<T, B, A> extends Xi<A, T, B> {
    }

    interface Foo<X> extends Zap<X, String, Integer> {
    }
}
