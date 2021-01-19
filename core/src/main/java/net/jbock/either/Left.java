package net.jbock.either;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class Left<L, R> extends Either<L, R> {

  private final L value;

  private static final Left<String, ?> NO_MESSAGE = new Left<>("no message");

  Left(L value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  static <__IGNORE> Left<String, __IGNORE> noMessage() {
    return (Left<String, __IGNORE>) NO_MESSAGE;
  }

  @SuppressWarnings("unchecked")
  private <R2> Left<L, R2> createLeft(L newValue) {
    if (newValue == value) {
      return (Left<L, R2>) this;
    }
    return new Left<>(newValue);
  }

  public L value() {
    return value;
  }

  @Override
  public boolean isPresent() {
    return false;
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, R2> rightMapper) {
    return createLeft(value);
  }

  @Override
  public Either<R, L> swap() {
    return right(value);
  }

  @Override
  public Either<L, Void> ifPresent(Consumer<R> rightConsumer) {
    return createLeft(value);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightMapper) {
    return createLeft(value);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Supplier<Either<L, R2>> rightMapper) {
    return createLeft(value);
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> leftMapper) {
    throw leftMapper.apply(value);
  }
}
