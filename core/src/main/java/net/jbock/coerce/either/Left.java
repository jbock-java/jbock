package net.jbock.coerce.either;

import java.util.function.Function;
import java.util.function.Predicate;

public class Left<L, R> extends Either<L, R> {

  private final L left;

  Left(L left) {
    this.left = left;
  }

  public L value() {
    return left;
  }

  @Override
  public <L2, R2> Either<L2, R2> map(Function<L, L2> leftFunction, Function<R, R2> rightFunction) {
    return left(leftFunction.apply(left));
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, R2> rightFunction) {
    return left(left);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightFunction) {
    return left(left);
  }

  @Override
  public R orElseThrow(Function<L, ? extends Throwable> f) {
    throw sneakyThrow(f.apply(left));
  }

  @Override
  public boolean failureMatches(Predicate<L> predicate) {
    return predicate.test(left);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
    throw (T) t;
  }
}
