package net.jbock.either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

final class Right<L, R> extends Either<L, R> {

  private static final Right<?, ?> NOTHING = new Right<>(null);

  private final R value;

  private Right(R value) {
    this.value = value;
  }

  static <L, R> Right<L, R> nothing() {
    @SuppressWarnings("unchecked")
    Right<L, R> result = (Right<L, R>) NOTHING;
    return result;
  }

  static <L, R> Right<L, R> create(R value) {
    if (value == null) {
      @SuppressWarnings("unchecked")
      Right<L, R> result = (Right<L, R>) NOTHING;
      return result;
    }
    return new Right<>(value);
  }

  @Override
  public boolean isPresent() {
    return true;
  }

  @Override
  public <R2> Either<L, R2> map(Function<? super R, ? extends R2> rightMapper) {
    return right(rightMapper.apply(value));
  }

  @Override
  public Either<R, L> swap() {
    return left(value);
  }

  @Override
  public void ifPresentOrElse(Consumer<R> rightConsumer, Consumer<L> leftConsumer) {
    rightConsumer.accept(value);
  }

  @Override
  public <R2> Either<L, R2> select(Function<? super R, ? extends Either<? extends L, ? extends R2>> choice) {
    @SuppressWarnings("unchecked")
    Either<L, R2> either = (Either<L, R2>) choice.apply(value);
    return either;
  }

  @Override
  public Either<L, R> filter(Function<? super R, ? extends Optional<? extends L>> fail) {
    @SuppressWarnings("unchecked")
    Optional<L> opt = (Optional<L>) fail.apply(value);
    return opt.<Either<L, R>>map(Either::left).orElse(this);
  }

  @Override
  public Either<L, R> maybeRecover(Function<? super L, ? extends Optional<? extends R>> choice) {
    return this;
  }

  @Override
  public R orRecover(Function<? super L, ? extends R> recover) {
    return value;
  }

  @Override
  public <X extends Throwable> R orElseThrow(Function<? super L, ? extends X> leftMapper) throws X {
    return value;
  }
}
