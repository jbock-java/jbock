package net.jbock.either;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class Right<L, R> extends Either<L, R> {

  private static final Right<?, Void> CONTAINS_NULL = new Right<>(null);

  @SuppressWarnings("unchecked")
  static <__IGNORE> Right<__IGNORE, Void> containsNull() {
    return (Right<__IGNORE, Void>) CONTAINS_NULL;
  }

  private final R value;

  Right(R value) {
    this.value = value;
  }

  @Override
  public boolean isPresent() {
    return true;
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, R2> rightMapper) {
    return right(rightMapper.apply(value));
  }

  @Override
  public Either<R, L> swap() {
    return left(value);
  }

  @Override
  public Either<L, Void> ifPresent(Consumer<R> rightConsumer) {
    rightConsumer.accept(value);
    return right();
  }

  @Override
  public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightMapper) {
    return rightMapper.apply(value);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Supplier<Either<L, R2>> rightMapper) {
    return rightMapper.get();
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> leftMapper) {
    return value;
  }
}
