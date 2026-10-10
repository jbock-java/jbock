package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * This class contains converters for all "auto types"
 * that can be used without a custom converter.
 */
// todo non static
public final class StandardConverters {

    private static final ConverterStore STORE = new ConverterStore();

    public static StringConverter<String> asString() {
        return STORE.asString();
    }

    public static StringConverter<Integer> asInteger() {
        return STORE.asInteger();
    }

    public static StringConverter<Long> asLong() {
        return STORE.asLong();
    }

    public static StringConverter<Short> asShort() {
        return STORE.asShort();
    }

    public static StringConverter<Byte> asByte() {
        return STORE.asByte();
    }

    public static StringConverter<Float> asFloat() {
        return STORE.asFloat();
    }

    public static StringConverter<Double> asDouble() {
        return STORE.asDouble();
    }

    public static StringConverter<Character> asCharacter() {
        return STORE.asCharacter();
    }

    public static StringConverter<Path> asPath() {
        return STORE.asPath();
    }

    public static StringConverter<URI> asURI() {
        return STORE.asURI();
    }

    public static StringConverter<Pattern> asPattern() {
        return STORE.asPattern();
    }

    public static StringConverter<LocalDate> asLocalDate() {
        return STORE.asLocalDate();
    }

    public static StringConverter<BigInteger> asBigInteger() {
        return STORE.asBigInteger();
    }

    public static StringConverter<BigDecimal> asBigDecimal() {
        return STORE.asBigDecimal();
    }

    public static StringConverter<File> asExistingFile() {
        return STORE.asExistingFile();
    }

    private StandardConverters() {
    }
}
