package net.jbock.contrib;

import net.jbock.util.StringConverter;

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

/**
 * This class contains converters for the "known types"
 * that can be used without a custom converter.
 */
// todo cache this
public final class StandardConverters {

    private final Map<String, StringConverter<?>> cache = new HashMap<>();

    private <T> StringConverter<T> get(
            Class<T> clazz,
            Function<String, T> function) {
        String canonicalName = clazz.getCanonicalName();
        StringConverter<?> cached = cache.get(canonicalName);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            StringConverter<T> result = (StringConverter<T>) cached;
            return result;
        }
        StringConverter<T> newInstance = StringConverter.create(function);
        cache.put(canonicalName, newInstance);
        return newInstance;
    }

    public StringConverter<Integer> asInteger() {
        return get(Integer.class, Integer::valueOf);
    }

    public StringConverter<Long> asLong() {
        return get(Long.class, Long::valueOf);
    }

    public StringConverter<Short> asShort() {
        return get(Short.class, Short::valueOf);
    }

    public StringConverter<Byte> asByte() {
        return get(Byte.class, Byte::valueOf);
    }

    public StringConverter<Float> asFloat() {
        return get(Float.class, Float::valueOf);
    }

    public StringConverter<Double> asDouble() {
        return get(Double.class, Double::valueOf);
    }

    public StringConverter<Character> asCharacter() {
        return get(Character.class, MoreConverters::asCharacter);
    }

    public StringConverter<Path> asPath() {
        return get(Path.class, Paths::get);
    }

    public StringConverter<URI> asURI() {
        return get(URI.class, URI::create);
    }

    public StringConverter<Pattern> asPattern() {
        return get(Pattern.class, Pattern::compile);
    }

    public StringConverter<LocalDate> asLocalDate() {
        return get(LocalDate.class, LocalDate::parse);
    }

    public StringConverter<BigInteger> asBigInteger() {
        return get(BigInteger.class, BigInteger::new);
    }

    public StringConverter<BigDecimal> asBigDecimal() {
        return get(BigDecimal.class, BigDecimal::new);
    }

    public StringConverter<File> asExistingFile() {
        return get(File.class, MoreConverters::existingFile);
    }
}
