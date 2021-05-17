package net.jbock.either;

import java.util.function.Function;

final class Right<L, R> extends Either<L, R> {

  private final R value;

  private Right(R value) {
    this.value = value;
  }

  static <L, R> Right<L, R> create(R value) {
    return new Right<>(value);
  }

  R value() {
    return value;
  }

  @Override
  Either<R, L> flip() {
    return left(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  <R2> Either<L, R2> flatMapInternal(
      Function<Right<L, R>, ? extends Either<? extends L, ? extends R2>> choice) {
    return (Either<L, R2>) choice.apply(this);
  }

  @Override
  public <U> U fold(
      Function<? super L, ? extends U> leftMapper,
      Function<? super R, ? extends U> rightMapper) {
    return rightMapper.apply(value);
  }
}
