package net.jbock.either;

import java.util.Optional;
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
  public Optional<L> getLeft() {
    return Optional.empty();
  }

  @Override
  public Optional<R> getRight() {
    return Optional.of(value);
  }

  @Override
  public <X extends Throwable> R orElseThrow(Function<? super L, ? extends X> f) throws X {
    return value;
  }

  @Override
  public <U> U fold(
      Function<? super L, ? extends U> leftMapper,
      Function<? super R, ? extends U> rightMapper) {
    return rightMapper.apply(value);
  }
}
