package net.jbock.either;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A collector that implements {@link Either#toValidListAll()}.
 *
 * @param <L> the LHS type
 * @param <R> the RHS type
 */
class ValidatingCollectorAll<L, R> implements Collector<Either<L, R>, ValidatingCollectorAll.Acc<L, R>, Either<List<L>, List<R>>> {

  static final class Acc<L, R> {

    private final List<L> left = new ArrayList<>();
    private final List<R> right = new ArrayList<>();

    void accumulate(Either<L, R> either) {
      if (left.isEmpty()) {
        either.accept(left::add, right::add);
      } else {
        either.acceptLeft(left::add);
      }
    }

    Acc<L, R> combine(Acc<L, R> other) {
      if (!left.isEmpty()) {
        left.addAll(other.left);
        return this;
      }
      if (!other.left.isEmpty()) {
        other.left.addAll(left);
        return other;
      }
      right.addAll(other.right);
      return this;
    }

    Either<List<L>, List<R>> finish() {
      if (left.isEmpty()) {
        return Either.right(right);
      } else {
        return Either.left(left);
      }
    }
  }

  @Override
  public Supplier<Acc<L, R>> supplier() {
    return Acc::new;
  }

  @Override
  public BiConsumer<Acc<L, R>, Either<L, R>> accumulator() {
    return Acc::accumulate;
  }

  @Override
  public BinaryOperator<Acc<L, R>> combiner() {
    return Acc::combine;
  }

  @Override
  public Function<Acc<L, R>, Either<List<L>, List<R>>> finisher() {
    return Acc::finish;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of();
  }
}
