package net.jbock.coerce.either;

import java.util.function.Function;

public class Right<L, R> extends Either<L, R> {

  private final R right;

  Right(R right) {
    this.right = right;
  }

  public R value() {
    return right;
  }

  @Override
  public <L2, R2> Either<L2, R2> map(Function<L, L2> leftMapper, Function<R, R2> rightMapper) {
    return right(rightMapper.apply(right));
  }

  @Override
  public <L2, R2> Either<L2, R2> flatMap(Function<L, L2> leftMapper, Function<R, Either<L2, R2>> rightMapper) {
    return rightMapper.apply(right);
  }

  @Override
  public <U> U fold(Function<L, U> leftMapper, Function<R, U> rightMapper) {
    return rightMapper.apply(right);
  }

  @Override
  public R orElseThrow(Function<L, ? extends Throwable> f) {
    return right;
  }
}
