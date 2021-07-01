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
 * A collector that implements {@link Either#toValidList()}.
 *
 * @param <L> the LHS type
 * @param <R> the RHS type
 */
class ValidatingCollector<L, R> implements Collector<Either<L, R>, ValidatingCollector.Acc<L, R>, Either<L, List<R>>> {

  static final class Acc<L, R> {

    private L left;

    private final List<R> right = new ArrayList<>();

    void accumulate(Either<L, R> either) {
      if (left != null) {
        return;
      }
      either.accept(l -> left = l,
          right::add);
    }

    Acc<L, R> combine(Acc<L, R> other) {
      if (left != null) {
        return this;
      }
      if (other.left != null) {
        return other;
      }
      right.addAll(other.right);
      return this;
    }

    Either<L, List<R>> finish() {
      if (left != null) {
        return Either.left(left);
      }
      return Either.right(right);
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
  public Function<Acc<L, R>, Either<L, List<R>>> finisher() {
    return Acc::finish;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of();
  }
}
