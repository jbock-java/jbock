package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

final class ConverterStore {

    private final Map<String, WeakReference<StringConverter<?>>> convertersByClass = new HashMap<>();

    <T> StringConverter<T> get(
            Class<T> clazz,
            Function<String, T> function) {
        return getCached(clazz, () -> StringConverter.create(function));
    }

    @SuppressWarnings("unchecked")
    private <T> StringConverter<T> getCached(
            Class<T> clazz,
            Supplier<StringConverter<T>> converterSupplier) {
        String canonicalName = clazz.getCanonicalName();
        WeakReference<StringConverter<?>> ref = convertersByClass.get(canonicalName);
        if (ref != null) {
            StringConverter<?> cached = ref.get();
            if (cached != null) {
                return (StringConverter<T>) cached;
            }
        }
        StringConverter<T> newInstance = converterSupplier.get();
        convertersByClass.put(canonicalName, new WeakReference<>(newInstance));
        return newInstance;
    }

}
