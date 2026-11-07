package net.jbock.contrib;

import net.jbock.util.Either;

import java.util.function.Function;

import static net.jbock.util.Either.left;
import static net.jbock.util.Either.right;

public final class EnumConverter {

    /**
     * Creates an instance of {@code EnumConverter}.
     *
     * @param valueOf reference of the {@code valueOf} method
     * @param <E> type of the enum class
     * @return an instance of {@code EnumConverter}
     */
    public static <E> Function<String, Either<String, E>> create(
            Function<String, E> valueOf) {
        return token -> {
            try {
                return right(valueOf.apply(token));
            } catch (Exception e) {
                return left("converter threw " + e.getClass().getCanonicalName() + ", message: " + e.getMessage());
            }
        };
    }
}
