package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;

/**
 * A {@code StringConverter} that converts to a given enum class.
 *
 * @param <E> type of the enum class
 */
public final class EnumConverter<E> extends StringConverter<E> {

    private final Function<String, E> valueOf;
    private final Supplier<List<E>> values;

    private EnumConverter(
            Function<String, E> valueOf,
            Supplier<List<E>> values) {
        this.valueOf = valueOf;
        this.values = values;
    }

    /**
     * Creates an instance of {@code EnumConverter}.
     *
     * @param valueOf reference to the {@code valueOf} enum method
     * @param values reference to the {@code values} enum method
     * @param <E> type of the enum class
     * @return an instance of {@code EnumConverter}
     */
    public static <E> StringConverter<E> create(
            Function<String, E> valueOf,
            Supplier<E[]> values) {
        return new EnumConverter<>(valueOf, () -> List.of(values.get()));
    }

    @Override
    protected E convert(String token) {
        try {
            return valueOf.apply(token);
        } catch (IllegalArgumentException e) {
            String strings = values.get().stream()
                    .map(Objects::toString)
                    .collect(joining("\n  ", "", "\n"));
            String message = e.getMessage() + "\nPossible values:\n  " + strings;
            throw new IllegalArgumentException(message);
        }
    }
}
