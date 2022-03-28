package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class StandardConverters {

    private static final Map<String, WeakReference<StringConverter<?>>> CONVERTERS_BY_CLASS = new HashMap<>();

    private static <T> StringConverter<T> getConverter(
            Class<T> clazz,
            Supplier<Function<String, T>> converterSupplier) {
        return getConverter2(clazz, () -> StringConverter.create(converterSupplier.get()));
    }

    @SuppressWarnings("unchecked")
    private static <T> StringConverter<T> getConverter2(
            Class<T> clazz,
            Supplier<StringConverter<T>> converterSupplier) {
        WeakReference<StringConverter<?>> stringConverter = CONVERTERS_BY_CLASS.computeIfAbsent(
                clazz.getCanonicalName(),
                canonicalName -> new WeakReference<>(converterSupplier.get()));
        StringConverter<T> result = (StringConverter<T>) stringConverter.get();
        if (result == null) {
            return (StringConverter<T>) StringConverter.create(converterSupplier.get());
        }
        return result;
    }

    public static StringConverter<String> asString() {
        return getConverter(String.class, Function::identity);
    }

    public static StringConverter<Path> asPath() {
        return getConverter(Path.class, () -> Paths::get);
    }

    public static StringConverter<File> asExistingFile() {
        return getConverter2(File.class, FileConverter::new);
    }
}
