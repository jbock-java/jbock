package net.jbock.common;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class Suppliers {

  private Suppliers() {
  }

  public static <T> Supplier<T> memoize(Supplier<T> delegate) {
    return Cache.of(delegate);
  }

  public static IntSupplier memoizeInt(IntSupplier delegate) {
    return new IntCache(delegate);
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

  private static final class IntCache implements IntSupplier {

    private boolean initialized;
    private int value;

    private final IntSupplier delegate;

    IntCache(IntSupplier delegate) {
      this.delegate = delegate;
    }

    @Override
    public int getAsInt() {
      if (!initialized) {
        initialized = true;
        value = delegate.getAsInt();
      }
      return value;
    }
  }
}
