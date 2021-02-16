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
  public Either<R, L> flip() {
    return left(value);
  }

  @Override
  <R2> Either<L, R2> flatMapInternal(Function<Right<L, R>, ? extends Either<? extends L, ? extends R2>> choice) {
    @SuppressWarnings("unchecked")
    Either<L, R2> either = (Either<L, R2>) choice.apply(this);
    return either;
  }

  @Override
  public <T> T fold(Function<? super L, ? extends T> leftMapper, Function<? super R, ? extends T> rightMapper) {
    return rightMapper.apply(value);
  }
}
