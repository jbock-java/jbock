package net.jbock.either;

import java.util.Optional;
import java.util.function.Function;

final class Left<L, R> extends Either<L, R> {

  private final L value;

  private Left(L value) {
    this.value = value;
  }

  static <L, R> Left<L, R> create(L value) {
    return new Left<>(value);
  }

  @Override
  public Either<R, L> flip() {
    return right(value);
  }

  @Override
  <R2> Either<L, R2> flatMapInternal(Function<Right<L, R>, ? extends Either<? extends L, ? extends R2>> choice) {
    @SuppressWarnings("unchecked")
    Left<L, R2> result = (Left<L, R2>) this;
    return result;
  }

  @Override
  public <T> T fold(Function<? super L, ? extends T> leftMapper, Function<? super R, ? extends T> rightMapper) {
    return leftMapper.apply(value);
  }
}
