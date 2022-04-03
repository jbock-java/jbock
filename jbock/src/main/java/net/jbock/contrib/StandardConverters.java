package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * This class contains converters for all "auto types"
 * that can be used without a custom converter.
 */
public final class StandardConverters {

    private static final ConverterStore STORE = new ConverterStore();

    public static StringConverter<String> asString() {
        return STORE.get(String.class, Function.identity());
    }

    public static StringConverter<Integer> asInteger() {
        return STORE.get(Integer.class, Integer::valueOf);
    }

    public static StringConverter<Long> asLong() {
        return STORE.get(Long.class, Long::valueOf);
    }

    public static StringConverter<Short> asShort() {
        return STORE.get(Short.class, Short::valueOf);
    }

    public static StringConverter<Byte> asByte() {
        return STORE.get(Byte.class, Byte::valueOf);
    }

    public static StringConverter<Float> asFloat() {
        return STORE.get(Float.class, Float::valueOf);
    }

    public static StringConverter<Double> asDouble() {
        return STORE.get(Double.class, Double::valueOf);
    }

    public static StringConverter<Character> asCharacter() {
        return STORE.get(Character.class, MoreConverters::asCharacter);
    }

    public static StringConverter<Path> asPath() {
        return STORE.get(Path.class, Paths::get);
    }

    public static StringConverter<URI> asURI() {
        return STORE.get(URI.class, URI::create);
    }

    public static StringConverter<Pattern> asPattern() {
        return STORE.get(Pattern.class, Pattern::compile);
    }

    public static StringConverter<LocalDate> asLocalDate() {
        return STORE.get(LocalDate.class, LocalDate::parse);
    }

    public static StringConverter<BigInteger> asBigInteger() {
        return STORE.get(BigInteger.class, BigInteger::new);
    }

    public static StringConverter<BigDecimal> asBigDecimal() {
        return STORE.get(BigDecimal.class, BigDecimal::new);
    }

    public static StringConverter<File> asExistingFile() {
        return STORE.get(File.class, MoreConverters::existingFile);
    }

    private StandardConverters() {
    }
}
