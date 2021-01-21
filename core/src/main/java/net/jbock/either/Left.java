package net.jbock.either;

import java.util.function.Consumer;
import java.util.function.Function;

final class Left<L, R> extends Either<L, R> {

  private final L value;

  private static final Left<?, ?> NOTHING = new Left<>(null);

  private Left(L value) {
    this.value = value;
  }

  static <L, R> Left<L, R> create(L value) {
    if (value == null) {
      return (Left<L, R>) NOTHING;
    }
    return new Left<>(value);
  }

  @SuppressWarnings("unchecked")
  static <L, R> Left<L, R> nothing() {
    return (Left<L, R>) NOTHING;
  }

  @SuppressWarnings("unchecked")
  private <R2> Left<L, R2> createLeft(L newValue) {
    if (newValue == value) {
      return (Left<L, R2>) this;
    }
    return create(newValue);
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
  public <R2> Either<L, R2> chooseRight(Function<R, Either<L, R2>> rightMapper) {
    return createLeft(value);
  }

  @Override
  public Either<L, R> maybeFail(Function<R, Either<L, ?>> maybe) {
    return createLeft(value);
  }

  @Override
  public <L2> Either<L2, R> mapLeft(Function<L, L2> leftMapper) {
    return left(leftMapper.apply(value));
  }

  @Override
  public <L2> Either<L2, R> chooseLeft(Function<L, Either<L2, R>> leftMapper) {
    return leftMapper.apply(value);
  }

  @Override
  public Either<L, R> maybeRecover(Function<L, Either<?, R>> maybe) {
    return maybe.apply(value).chooseLeft(v -> this);
  }

  @Override
  public R orElse(Function<L, R> leftMapper) {
    return leftMapper.apply(value);
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> leftMapper) {
    throw leftMapper.apply(value);
  }
}
