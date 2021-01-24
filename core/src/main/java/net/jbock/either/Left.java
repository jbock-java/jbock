package net.jbock.either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

final class Left<L, R> extends Either<L, R> {

  private final L value;

  private Left(L value) {
    this.value = value;
  }

  static <L, R> Left<L, R> create(L value) {
    return new Left<>(value);
  }

  private <R2> Left<L, R2> theSame() {
    @SuppressWarnings("unchecked")
    Left<L, R2> result = (Left<L, R2>) this;
    return result;
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
    return theSame();
  }

  @Override
  public Either<R, L> swap() {
    return right(value);
  }

  @Override
  public void ifPresentOrElse(Consumer<? super R> action, Consumer<? super L> leftAction) {
    leftAction.accept(value);
  }

  @Override
  public <R2> Either<L, R2> select(Function<? super R, ? extends Either<? extends L, ? extends R2>> choice) {
    return theSame();
  }

  @Override
  public Either<L, R> filter(Function<? super R, ? extends Optional<? extends L>> fail) {
    return this;
  }

  @Override
  public R orRecover(Function<? super L, ? extends R> recover) {
    return recover.apply(value);
  }
}
