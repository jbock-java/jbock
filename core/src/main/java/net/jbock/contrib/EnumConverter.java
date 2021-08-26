package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;

public final class EnumConverter<E> extends StringConverter<E> {

    private final Function<String, E> valueOf;
    private final Supplier<List<E>> values;

    private EnumConverter(
            Function<String, E> valueOf,
            Supplier<List<E>> values) {
        this.valueOf = valueOf;
        this.values = values;
    }

    public static <E> EnumConverter<E> create(
            Function<String, E> valueOf,
            Supplier<List<E>> values) {
        return new EnumConverter<>(valueOf, values);
    }

    @Override
    protected E convert(String token) {
        try {
            return valueOf.apply(token);
        } catch (IllegalArgumentException e) {
            String strings = values.get().stream()
                    .map(Objects::toString)
                    .collect(joining(", ", "[", "]"));
            String message = e.getMessage() + " " + strings;
            throw new IllegalArgumentException(message);
        }
    }
}
