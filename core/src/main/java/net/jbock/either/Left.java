package net.jbock.either;

import java.util.function.Consumer;
import java.util.function.Function;

final class Left<L, R> extends Either<L, R> {

  private static final Left<?, ?> NOTHING = new Left<>(null);

  private final L value;

  private Left(L value) {
    this.value = value;
  }

  static <L, R> Left<L, R> nothing() {
    @SuppressWarnings("unchecked")
    Left<L, R> result = (Left<L, R>) NOTHING;
    return result;
  }

  static <L, R> Left<L, R> create(L value) {
    if (value == null) {
      @SuppressWarnings("unchecked")
      Left<L, R> result = (Left<L, R>) NOTHING;
      return result;
    }
    return new Left<>(value);
  }

  private <R2> Left<L, R2> createIfNecessary(L newValue) {
    if (newValue == value) {
      @SuppressWarnings("unchecked")
      Left<L, R2> result = (Left<L, R2>) this;
      return result;
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
  public <R2> Either<L, R2> map(Function<? super R, ? extends R2> rightMapper) {
    return createIfNecessary(value);
  }

  @Override
  public Either<R, L> swap() {
    return right(value);
  }

  @Override
  public void ifPresentOrElse(Consumer<R> rightConsumer, Consumer<L> leftConsumer) {
    leftConsumer.accept(value);
  }

  @Override
  public <R2> Either<L, R2> chooseRight(Function<? super R, ? extends Either<? extends L, ? extends R2>> choice) {
    return createIfNecessary(value);
  }

  @Override
  public Either<L, R> maybeFail(Function<? super R, ? extends Either<? extends L, ?>> choice) {
    return createIfNecessary(value);
  }

  @Override
  public <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> leftMapper) {
    return left(leftMapper.apply(value));
  }

  @Override
  public <L2> Either<L2, R> chooseLeft(Function<? super L, ? extends Either<? extends L2, ? extends R>> leftMapper) {
    @SuppressWarnings("unchecked")
    Either<L2, R> result = (Either<L2, R>) leftMapper.apply(value);
    return result;
  }

  @Override
  public Either<L, R> maybeRecover(Function<? super L, ? extends Either<?, ? extends R>> choice) {
    @SuppressWarnings("unchecked")
    Either<?, R> either = (Either<?, R>) choice.apply(value);
    return either.chooseLeft(v -> this);
  }

  @Override
  public R orRecover(Function<? super L, ? extends R> recover) {
    return recover.apply(value);
  }

  @Override
  public <X extends Throwable> R orElseThrow(Function<? super L, ? extends X> leftMapper) throws X {
    throw leftMapper.apply(value);
  }
}
