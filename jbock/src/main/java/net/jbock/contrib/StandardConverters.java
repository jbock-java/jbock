package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class StandardConverters {

    private static final Map<String, WeakReference<StringConverter<?>>> CONVERTERS_BY_CLASS = new HashMap<>();

    private static <T> StringConverter<T> get(
            Class<T> clazz,
            Function<String, T> f) {
        return get(clazz, () -> StringConverter.create(f));
    }

    @SuppressWarnings("unchecked")
    private static <T> StringConverter<T> get(
            Class<T> clazz,
            Supplier<StringConverter<T>> converterSupplier) {
        String canonicalName = clazz.getCanonicalName();
        WeakReference<StringConverter<?>> ref = CONVERTERS_BY_CLASS.get(canonicalName);
        if (ref != null) {
            StringConverter<?> cached = ref.get();
            if (cached != null) {
                return (StringConverter<T>) cached;
            }
        }
        StringConverter<T> newInstance = converterSupplier.get();
        CONVERTERS_BY_CLASS.put(canonicalName, new WeakReference<>(newInstance));
        return newInstance;
    }

    public static StringConverter<String> asString() {
        return get(String.class, Function.identity());
    }

    public static StringConverter<Integer> asInteger() {
        return get(Integer.class, Integer::valueOf);
    }

    public static StringConverter<Long> asLong() {
        return get(Long.class, Long::valueOf);
    }

    public static StringConverter<Short> asShort() {
        return get(Short.class, Short::valueOf);
    }

    public static StringConverter<Byte> asByte() {
        return get(Byte.class, Byte::valueOf);
    }

    public static StringConverter<Float> asFloat() {
        return get(Float.class, Float::valueOf);
    }

    public static StringConverter<Double> asDouble() {
        return get(Double.class, Double::valueOf);
    }

    public static StringConverter<Character> asCharacter() {
        return get(Character.class, CharConverter::new);
    }

    public static StringConverter<Path> asPath() {
        return get(Path.class, Paths::get);
    }

    public static StringConverter<URI> asURI() {
        return get(URI.class, URI::create);
    }

    public static StringConverter<Pattern> asPattern() {
        return get(Pattern.class, Pattern::compile);
    }

    public static StringConverter<LocalDate> asLocalDate() {
        return get(LocalDate.class, LocalDate::parse);
    }

    public static StringConverter<BigInteger> asBigInteger() {
        return get(BigInteger.class, BigInteger::new);
    }

    public static StringConverter<BigDecimal> asBigDecimal() {
        return get(BigDecimal.class, BigDecimal::new);
    }

    public static StringConverter<File> asExistingFile() {
        return get(File.class, FileConverter::new);
    }
}
