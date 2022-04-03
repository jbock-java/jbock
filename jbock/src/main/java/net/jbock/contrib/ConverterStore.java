package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class ConverterStore {

    private final Map<String, WeakReference<StringConverter<?>>> convertersByClass = new HashMap<>();

    @SuppressWarnings("unchecked")
    <T> StringConverter<T> get(
            Class<T> clazz,
            Function<String, T> function) {
        String canonicalName = clazz.getCanonicalName();
        WeakReference<StringConverter<?>> ref = convertersByClass.get(canonicalName);
        if (ref != null) {
            StringConverter<?> cached = ref.get();
            if (cached != null) {
                return (StringConverter<T>) cached;
            }
        }
        StringConverter<T> newInstance = StringConverter.create(function);
        convertersByClass.put(canonicalName, new WeakReference<>(newInstance));
        return newInstance;
    }
}
