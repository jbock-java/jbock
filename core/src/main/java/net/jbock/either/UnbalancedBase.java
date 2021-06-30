package net.jbock.either;

import java.util.Optional;

public abstract class UnbalancedBase<T> {

  final Optional<? extends T> value;

  UnbalancedBase(Optional<? extends T> value) {
    this.value = value;
  }

  /**
   * If a value is present, returns true, otherwise false.
   *
   * @return {@code true} if a value is present, otherwise {@code false}
   */
  public final boolean isPresent() {
    return value.isPresent();
  }
}
