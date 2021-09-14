package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;

/**
 * A {@code StringConverter} that converts to a given enum class.
 *
 * @param <E> type of the enum class
 */
public final class EnumConverter<E> extends StringConverter<E> {

    private final E[] values;

    private EnumConverter(E[] values) {
        this.values = values;
    }

    /**
     * Creates an instance of {@code EnumConverter}.
     *
     * @param values reference to the {@code values} enum method
     * @param <E> type of the enum class
     * @return an instance of {@code EnumConverter}
     */
    public static <E> StringConverter<E> create(
            Supplier<E[]> values) {
        return new EnumConverter<>(values.get());
    }

    @Override
    protected E convert(String token) {
        for (E value : values) {
            if (Objects.toString(value, "").equalsIgnoreCase(token)) {
                return value;
            }
        }
        String strings = Arrays.stream(values)
                .map(Objects::toString)
                .collect(joining("\n  ", "", "\n"));
        String message = "No such constant: " + token.toUpperCase(Locale.US) +
                "\nPossible values (ignoring case):\n  " + strings;
        throw new RuntimeException(message);
    }
}
