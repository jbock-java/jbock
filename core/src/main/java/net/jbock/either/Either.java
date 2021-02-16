package net.jbock.either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return Left.create(value);
  }

  public static <L, R> Either<L, R> right(R value) {
    return Right.create(value);
  }

  public static <L, R> Either<L, R> fromSuccess(L failure, Optional<? extends R> success) {
    return fromSuccessGet(() -> failure, success);
  }

  public static <L, R> Either<L, R> fromSuccessGet(Supplier<? extends L> failure, Optional<? extends R> success) {
    return success.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(failure.get()));
  }

  public static <L, R> Either<L, R> fromFailure(Optional<? extends L> failure, R success) {
    return fromFailureGet(failure, () -> success);
  }

  public static <L, R> Either<L, R> fromFailureGet(Optional<? extends L> failure, Supplier<? extends R> success) {
    return failure.<Either<L, R>>map(Left::create)
        .orElseGet(() -> Right.create(success.get()));
  }

  abstract <R2> Either<L, R2> flatMapInternal(
      Function<Right<L, R>, ? extends Either<? extends L, ? extends R2>> choice);

  public final <R2> Either<L, R2> map(Function<? super R, ? extends R2> rightMapper) {
    return flatMapInternal(r -> right(rightMapper.apply(r.value())));
  }

  public final <R2> Either<L, R2> flatMap(
      Function<? super R, ? extends Either<? extends L, ? extends R2>> choice) {
    return flatMapInternal(right -> choice.apply(right.value()));
  }

  public final <R2> Either<L, R2> flatMap(Supplier<? extends Either<? extends L, ? extends R2>> choice) {
    return flatMapInternal(r -> choice.get());
  }

  public final Either<L, R> filter(Function<? super R, ? extends Optional<? extends L>> fail) {
    return flatMapInternal(r -> fail.apply(r.value()).<Either<L, R>>map(Either::left).orElse(r));
  }

  public final Either<L, R> filter(Supplier<? extends Optional<? extends L>> fail) {
    return filter(r -> fail.get());
  }

  public final <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> leftMapper) {
    @SuppressWarnings("unchecked")
    Either<L2, R> result = (Either<L2, R>) flip().map(leftMapper).flip();
    return result;
  }

  public final Either<L, R> maybeRecover(Function<? super L, ? extends Optional<? extends R>> choice) {
    return flip().filter(choice).flip();
  }

  public final Either<L, R> maybeRecover(Supplier<? extends Optional<? extends R>> choice) {
    return maybeRecover(value -> choice.get());
  }

  public abstract <T> T fold(
      Function<? super L, ? extends T> leftMapper,
      Function<? super R, ? extends T> rightMapper);

  public final R orElse(Function<? super L, ? extends R> recover) {
    return fold(recover, Function.identity());
  }

  public abstract Either<R, L> flip();

  public final void accept(Consumer<? super L> leftAction, Consumer<? super R> rightAction) {
    fold(l -> {
      leftAction.accept(l);
      return null;
    }, r -> {
      rightAction.accept(r);
      return null;
    });
  }
}
