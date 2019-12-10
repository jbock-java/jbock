package net.jbock.coerce.either;

import java.util.function.Function;
import java.util.function.Predicate;

public class Right<L, R> extends Either<L, R> {

  private final R right;

  Right(R right) {
    this.right = right;
  }

  public R value() {
    return right;
  }

  @Override
  public <L2, R2> Either<L2, R2> map(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
    return right(rightFunction.apply(right));
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, R2> rightFunction) {
    return right(rightFunction.apply(right));
  }

  @Override
  public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightFunction) {
    return rightFunction.apply(right);
  }

  @Override
  public R orElseThrow(Function<L, ? extends Throwable> f) {
    return right;
  }

  @Override
  public boolean failureMatches(Predicate<L> predicate) {
    return false;
  }
}
