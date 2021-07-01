package net.jbock.either;

import java.util.NoSuchElementException;
import java.util.Objects;

abstract class UnbalancedBase<T> {

  private final T value;

  UnbalancedBase(T value) {
    this.value = value;
  }

  /**
   * If a value is present, returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is present, otherwise {@code false}
   */
  public final boolean isPresent() {
    return value != null;
  }

  /**
   * If a value is  not present, returns {@code true}, otherwise
   * {@code false}.
   *
   * @return {@code true} if a value is not present, otherwise {@code false}
   */
  public final boolean isEmpty() {
    return value == null;
  }

  T unsafeGet() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  boolean isEqual(UnbalancedBase<?> other) {
    return Objects.equals(value, other.value);
  }

  /**
   * Returns the hash code of the value, if present, otherwise {@code 0}
   * (zero) if no value is present.
   *
   * @return hash code value of the present value or {@code 0} if no value is
   *         present
   */
  public final int hashCode() {
    return Objects.hashCode(value);
  }
}
