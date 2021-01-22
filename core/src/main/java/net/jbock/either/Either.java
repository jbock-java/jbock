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

  public static <L, R> Either<L, R> fromOptionalSuccess(Supplier<L> failure, Optional<R> maybeSuccess) {
    return maybeSuccess.<Either<L, R>>map(Right::create)
        .orElseGet(() -> Left.create(failure.get()));
  }

  public static <L, R> Either<L, R> fromOptionalFailure(Supplier<R> success, Optional<L> maybeFailure) {
    return maybeFailure.<Either<L, R>>map(Left::create)
        .orElseGet(() -> Right.create(success.get()));
  }

  public abstract boolean isPresent();

  public abstract <R2> Either<L, R2> map(Function<R, R2> rightMapper);

  public abstract <R2> Either<L, R2> chooseRight(Function<R, Either<L, R2>> choice);

  public abstract Either<L, R> maybeFail(Function<R, Either<L, ?>> choice);

  public final Either<L, R> maybeFail(Supplier<Either<L, ?>> choice) {
    return maybeFail(r -> choice.get());
  }

  public abstract <L2> Either<L2, R> mapLeft(Function<L, L2> leftMapper);

  public abstract <L2> Either<L2, R> chooseLeft(Function<L, Either<L2, R>> leftMapper);

  public abstract Either<L, R> maybeRecover(Function<L, Either<?, R>> choice);

  public final Either<L, R> maybeRecover(Supplier<Either<?, R>> choice) {
    return maybeRecover(l -> choice.get());
  }

  public abstract R orRecover(Function<L, R> success);

  public final R orRecover(Supplier<R> success) {
    return orRecover(left -> success.get());
  }

  public abstract Either<R, L> swap();

  public abstract Either<L, Void> ifPresent(Consumer<R> rightConsumer);

  public abstract R orElseThrow(Function<L, ? extends RuntimeException> leftMapper);
}
