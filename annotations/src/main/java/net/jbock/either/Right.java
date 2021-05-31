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

  @Override
  Either<R, L> flip() {
    return left(value);
  }

  @Override
  public <U> U fold(
      Function<? super L, ? extends U> leftMapper,
      Function<? super R, ? extends U> rightMapper) {
    return rightMapper.apply(value);
  }
}
