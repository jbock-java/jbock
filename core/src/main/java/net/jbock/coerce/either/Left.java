package net.jbock.coerce.either;

import java.util.function.Function;
import java.util.function.Supplier;

public class Left<L, R> extends Either<L, R> {

  private final L left;

  Left(L left) {
    this.left = left;
  }

  public L value() {
    return left;
  }

  @Override
  public <R2> Either<L, R2> map(Function<R, R2> rightMapper) {
    return left(left);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> rightMapper) {
    return left(left);
  }

  @Override
  public <R2> Either<L, R2> flatMap(Supplier<Either<L, R2>> rightMapper) {
    return left(left);
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> leftMapper) {
    throw leftMapper.apply(left);
  }
}
