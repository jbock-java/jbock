package net.jbock.either;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  public static <R> Either<String, R> left() {
    return Left.noMessage();
  }

  public static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  public static <L> Either<L, Void> right() {
    return Right.containsNull();
  }

  public abstract boolean isPresent();

  public abstract <R2> Either<L, R2> map(Function<R, R2> rightMapper);

  public abstract <R2> Either<L, R2> fail(Function<R, L> rightMapper);

  public abstract Either<L, Void> ifPresent(Consumer<R> rightConsumer);

  public abstract <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightMapper);

  public abstract <R2> Either<L, R2> flatMap(Supplier<Either<L, R2>> rightMapper);

  public abstract R orElseThrow(Function<L, ? extends RuntimeException> leftMapper);
}
