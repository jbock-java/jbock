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
  Either<R, L> flip() {
    return right(value);
  }

  @Override
  public UnbalancedLeft<L> getLeft() {
    return UnbalancedLeft.of(Optional.of(value));
  }

  @Override
  public boolean isLeft() {
    return true;
  }

  @Override
  public UnbalancedRight<R> getRight() {
    return UnbalancedRight.empty();
  }

  @Override
  public <X extends Throwable> R orElseThrow(Function<? super L, ? extends X> f) throws X {
    throw f.apply(value);
  }

  @Override
  public <U> U fold(
      Function<? super L, ? extends U> leftMapper,
      Function<? super R, ? extends U> rightMapper) {
    return leftMapper.apply(value);
  }
}
