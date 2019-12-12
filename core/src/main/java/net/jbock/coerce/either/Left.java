package net.jbock.coerce.either;

import java.util.function.Function;

public class Left<L, R> extends Either<L, R> {

  private final L left;

  Left(L left) {
    this.left = left;
  }

  public L value() {
    return left;
  }

  @Override
  public <L2, R2> Either<L2, R2> map(Function<L, L2> leftMapper, Function<R, R2> rightMapper) {
    return left(leftMapper.apply(left));
  }

  @Override
  public <L2, R2> Either<L2, R2> flatMap(Function<L, L2> leftMapper, Function<R, Either<L2, R2>> rightMapper) {
    return left(leftMapper.apply(left));
  }

  @Override
  public <U> U fold(Function<L, U> leftMapper, Function<R, U> rightMapper) {
    return leftMapper.apply(left);
  }

  @Override
  public R orElseThrow(Function<L, ? extends RuntimeException> f) {
    throw f.apply(left);
  }
}
