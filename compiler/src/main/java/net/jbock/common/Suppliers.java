package net.jbock.common;

import java.util.function.Supplier;

public final class Suppliers {

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        return Cache.of(delegate);
    }

    private static final class Cache<T> implements Supplier<T> {

        private boolean initialized;
        private T value;

        private final Supplier<T> delegate;

        Cache(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        static <T> Cache<T> of(Supplier<T> supplier) {
            return new Cache<>(supplier);
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
