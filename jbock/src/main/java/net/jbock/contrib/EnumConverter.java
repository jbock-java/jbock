package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.util.Arrays;
import java.util.Locale;
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
    private final E[] values;

    private EnumConverter(
            Function<String, E> valueOf,
            E[] values) {
        this.valueOf = valueOf;
        this.values = values;
    }

    /**
     * Creates an instance of {@code EnumConverter}.
     *
     * @param valueOf reference of the {@code valueOf} method
     * @param values reference of the {@code values} method
     * @param <E> type of the enum class
     * @return an instance of {@code EnumConverter}
     */
    public static <E> StringConverter<E> create(
            Function<String, E> valueOf,
            Supplier<E[]> values) {
        return new EnumConverter<>(valueOf, values.get());
    }

    @Override
    protected E convert(String token) {
        try {
            return valueOf.apply(token);
        } catch (IllegalArgumentException e) {
            return tryCaseInsensitive(token);
        }
    }

    private E tryCaseInsensitive(String token) {
        for (E value : values) {
            if (Objects.toString(value, "").equalsIgnoreCase(token)) {
                return value;
            }
        }
        String strings = Arrays.stream(values)
                .map(Objects::toString)
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(joining("\n  ", "", "\n"));
        String message = "No such constant: " + token.toUpperCase(Locale.ROOT) +
                "\nPossible values (ignoring case):\n  " + strings;
        throw new RuntimeException(message);
    }
}
