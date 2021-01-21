package net.jbock.either;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return Left.create(value);
  }

  public static <L, R> Either<L, R> left() {
    return Left.nothing();
  }

  public static <L, R> Either<L, R> right(R value) {
    return Right.create(value);
  }

  public static <L> Either<L, Void> right() {
    return Right.containsNull();
  }

  public static <L, R> Either<L, R> fromOptional(L emptyValue, Optional<R> optional) {
    return fromOptional(() -> emptyValue, optional);
  }

  public static <L, R> Either<L, R> fromOptional(Supplier<L> emptyValue, Optional<R> optional) {
    return optional.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(emptyValue.get()));
  }

  public abstract boolean isPresent();

  public abstract <R2> Either<L, R2> map(Function<R, R2> rightMapper);

  public abstract <R2> Either<L, R2> chooseRight(Function<R, Either<L, R2>> rightMapper);

  public final <R2> Either<L, R2> maybeFail(Supplier<Either<L, R2>> rightMapper) {
    return chooseRight(right -> rightMapper.get());
  }

  public abstract <L2> Either<L2, R> mapLeft(Function<L, L2> leftMapper);

  public abstract <L2> Either<L2, R> chooseLeft(Function<L, Either<L2, R>> leftMapper);

  public final Either<L, R> maybeRecover(Supplier<Optional<R>> maybe) {
    return maybe.get().<Either<L, R>>map(Right::create).orElse(this);
  }

  public abstract R orElse(Function<L, R> leftMapper);

  public final R orElse(Supplier<R> supplier) {
    return orElse(left -> supplier.get());
  }

  public abstract Either<R, L> swap();

  public abstract Either<L, Void> ifPresent(Consumer<R> rightConsumer);

  public abstract R orElseThrow(Function<L, ? extends RuntimeException> leftMapper);
}
