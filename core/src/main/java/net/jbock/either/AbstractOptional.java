package net.jbock.either;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container object which may or may not contain a non-{@code null} value.
 *
 * @param <T> the type of the value
 */
public abstract class AbstractOptional<T> {

  private final T value;

  AbstractOptional(T value) {
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
   * If a value is present, returns {@code false}, otherwise {@code true}.
   *
   * @return {@code false} if a value is present, otherwise {@code true}
   */
  public final boolean isEmpty() {
    return value == null;
  }

  boolean isEqual(AbstractOptional<?> other) {
    return Objects.equals(value, other.value);
  }

  /**
   * If a value is present, performs the given action with the value,
   * otherwise does nothing.
   *
   * @param action the action to be performed, if a value is present
   */
  public final void ifPresent(Consumer<? super T> action) {
    if (isPresent()) {
      action.accept(orElseThrow());
    }
  }

  /**
   * If a value is present, performs the given action with the value,
   * otherwise performs the given empty-based action.
   *
   * @param action the action to be performed, if a value is present
   * @param emptyAction the empty-based action to be performed, if no value is
   *        present
   */
  public final void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
    if (isPresent()) {
      action.accept(orElseThrow());
    } else {
      emptyAction.run();
    }
  }

  /**
   * If a value is present, returns a sequential {@link Stream} containing
   * only that value, otherwise returns an empty {@code Stream}.
   *
   * @return the optional value as a {@code Stream}
   */
  public final Stream<T> stream() {
    if (isPresent()) {
      return Stream.of(orElseThrow());
    }
    return Stream.empty();
  }

  /**
   * If a value is present, returns the value, otherwise returns
   * {@code other}.
   *
   * @param other the value to be returned, if no value is present.
   * @return the value, if present, otherwise {@code other}
   */
  public final T orElse(T other) {
    if (isPresent()) {
      return orElseThrow();
    }
    return other;
  }

  /**
   * If a value is present, returns the value, otherwise returns the result
   * produced by the supplying function.
   *
   * @param supplier the supplying function that produces a value to be returned
   * @return the value, if present, otherwise the result produced by the
   *         supplying function
   */
  public final T orElseGet(Supplier<? extends T> supplier) {
    if (isPresent()) {
      return orElseThrow();
    }
    return supplier.get();
  }

  /**
   * If a value is present, returns the value, otherwise throws
   * {@code NoSuchElementException}.
   *
   * @return the non-{@code null} value described by this {@code Optional}
   * @throws NoSuchElementException if no value is present
   */
  public final T orElseThrow() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  /**
   * If a value is present, returns the value, otherwise throws an exception
   * produced by the exception supplying function.
   *
   * @param <X> Type of the exception to be thrown
   * @param exceptionSupplier the supplying function that produces an
   *        exception to be thrown
   * @return the value, if present
   * @throws X if no value is present
   */
  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (value != null) {
      return value;
    }
    throw exceptionSupplier.get();
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
