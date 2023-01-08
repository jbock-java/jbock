package net.jbock.util;

import java.util.function.Supplier;

public final class Suppliers {

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        return new Cache<>(delegate);
    }

    private static final class Cache<T> implements Supplier<T> {

        boolean initialized;
        T value;

        final Supplier<T> delegate;

        Cache(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            if (!initialized) {
                initialized = true;
                value = delegate.get();
            }
            return value;
        }
    }

    private Suppliers() {
    }
}
