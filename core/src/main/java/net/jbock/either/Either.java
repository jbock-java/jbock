package net.jbock.either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return Left.create(value);
  }

  public static <L, R> Either<L, R> right(R value) {
    return Right.create(value);
  }

  public static <R> RightOptional<R> maybeRight(Optional<? extends R> right) {
    return new RightOptional<>(right);
  }

  public static <L> LeftOptional<L> maybeLeft(Optional<? extends L> left) {
    return new LeftOptional<>(left);
  }

  public final <R2> Either<L, R2> map(Function<? super R, ? extends R2> rightMapper) {
    return flatMap(r -> right(rightMapper.apply(r)));
  }

  public final <R2> Either<L, R2> flatMap(
      Function<? super R, ? extends Either<? extends L, ? extends R2>> choice) {
    return fold(Either::left, r -> narrow(choice.apply(r)));
  }

  public final <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> leftMapper) {
    return narrow(flip().map(leftMapper).flip());
  }

  public final <L2> Either<L2, R> flatMapLeft(
      Function<? super L, ? extends Either<? extends L2, ? extends R>> choice) {
    return narrow(flip().flatMap(l -> choice.apply(l).flip()).flip());
  }

  public final boolean isRight() {
    return fold(l -> false, r -> true);
  }

  public abstract <U> U fold(
      Function<? super L, ? extends U> leftMapper,
      Function<? super R, ? extends U> rightMapper);

  abstract Either<R, L> flip();

  public final void accept(Consumer<? super L> leftAction, Consumer<? super R> rightAction) {
    fold(l -> {
      leftAction.accept(l);
      return null;
    }, r -> {
      rightAction.accept(r);
      return null;
    });
  }

  @SuppressWarnings("unchecked")
  private static <L, R> Either<L, R> narrow(Either<? extends L, ? extends R> either) {
    return (Either<L, R>) either;
  }
}
