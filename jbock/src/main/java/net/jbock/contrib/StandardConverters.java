package net.jbock.contrib;

import net.jbock.util.Either;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static net.jbock.util.Either.left;
import static net.jbock.util.Either.right;

/**
 * This class converts from {@code String} to certain &quot;known types&quot;.
 */
public final class StandardConverters {

    private final Map<String, Function<String, ?>> cache = new HashMap<>();

    private <T> Function<String, Either<String, T>> get(
            Class<T> clazz,
            Function<String, T> function) {
        String canonicalName = clazz.getCanonicalName();
        Function<String, ?> cached = cache.get(canonicalName);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            Function<String, Either<String, T>> result = (Function<String, Either<String, T>>) cached;
            return result;
        }
        Function<String, Either<String, T>> newInstance = token -> {
            try {
                T result = function.apply(token);
                if (result == null) {
                    return left("Converter returned null");
                }
                return right(result);
            } catch (Exception e) {
                return left("Converter threw " + e.getClass() + ", message: " + e.getMessage());
            }
        };
        cache.put(canonicalName, newInstance);
        return newInstance;
    }

    public Function<String, Either<String, Integer>> asInteger() {
        return get(Integer.class, Integer::valueOf);
    }

    public Function<String, Either<String, Long>> asLong() {
        return get(Long.class, Long::valueOf);
    }

    public Function<String, Either<String, Short>> asShort() {
        return get(Short.class, Short::valueOf);
    }

    public Function<String, Either<String, Byte>> asByte() {
        return get(Byte.class, Byte::valueOf);
    }

    public Function<String, Either<String, Float>> asFloat() {
        return get(Float.class, Float::valueOf);
    }

    public Function<String, Either<String, Double>> asDouble() {
        return get(Double.class, Double::valueOf);
    }

    public Function<String, Either<String, Character>> asCharacter() {
        return get(Character.class, MoreConverters::asCharacter);
    }

    public Function<String, Either<String, Path>> asPath() {
        return get(Path.class, Paths::get);
    }

    public Function<String, Either<String, URI>> asURI() {
        return get(URI.class, URI::create);
    }

    public Function<String, Either<String, Pattern>> asPattern() {
        return get(Pattern.class, Pattern::compile);
    }

    public Function<String, Either<String, LocalDate>> asLocalDate() {
        return get(LocalDate.class, LocalDate::parse);
    }

    public Function<String, Either<String, BigInteger>> asBigInteger() {
        return get(BigInteger.class, BigInteger::new);
    }

    public Function<String, Either<String, BigDecimal>> asBigDecimal() {
        return get(BigDecimal.class, BigDecimal::new);
    }

    public Function<String, Either<String, File>> asExistingFile() {
        return get(File.class, MoreConverters::existingFile);
    }
}
