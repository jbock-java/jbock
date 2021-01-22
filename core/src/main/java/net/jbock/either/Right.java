package net.jbock.either;

import java.util.function.Consumer;
import java.util.function.Function;

final class Right<L, R> extends Either<L, R> {

  private static final Right<?, Void> NOTHING = new Right<>(null);

  @SuppressWarnings("unchecked")
  static <__IGNORE> Right<__IGNORE, Void> containsNull() {
    return (Right<__IGNORE, Void>) NOTHING;
  }

  private final R value;

  private Right(R value) {
    this.value = value;
  }

  static <L, R> Right<L, R> create(R value) {
    if (value == null) {
      return (Right<L, R>) NOTHING;
    }
    return new Right<>(value);
  }

  @SuppressWarnings("unchecked")
  private <L2> Right<L2, R> createRight(R newValue) {
    if (newValue == value) {
      return (Right<L2, R>) this;
    }
    return create(newValue);
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
  public <R2> Either<L, R2> chooseRight(Function<R, Either<L, R2>> choice) {
    return choice.apply(value);
  }

  @Override
  public Either<L, R> maybeFail(Function<R, Either<L, ?>> choice) {
    return choice.apply(value).chooseRight(v -> this);
  }

  @Override
  public <L2> Either<L2, R> mapLeft(Function<L, L2> leftMapper) {
    return createRight(value);
  }

  @Override
  public <L2> Either<L2, R> chooseLeft(Function<L, Either<L2, R>> leftMapper) {
    return createRight(value);
  }

  @Override
  public Either<L, R> maybeRecover(Function<L, Either<?, R>> choice) {
    return createRight(value);
  }

  @Override
  public R orRecover(Function<L, R> leftMapper) {
    return value;
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> leftMapper) {
    return value;
  }
}
