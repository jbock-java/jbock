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

  public static <R> LeftOptional<R> ofRight(Optional<? extends R> right) {
    return new LeftOptional<>(right);
  }

  public static <L> RightOptional<L> ofLeft(Optional<? extends L> left) {
    return new RightOptional<>(left);
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

  public final boolean isRight() {
    return fold(l -> false, r -> true);
  }

  public abstract <T> T fold(
      Function<? super L, ? extends T> leftMapper,
      Function<? super R, ? extends T> rightMapper);

  public final R orElseGet(Function<? super L, ? extends R> recover) {
    return fold(recover, Function.identity());
  }

  public final R orElse(R defaultValue) {
    return orElseGet(l -> defaultValue);
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
