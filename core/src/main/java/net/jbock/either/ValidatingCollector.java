package net.jbock.either;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

class ValidatingCollector<L, R> implements Collector<Either<L, R>, ValidatingCollector.Acc<L, R>, Either<L, List<R>>> {

  static final class Acc<L, R> {

    private L left;
    private final List<R> values = new ArrayList<>();

    void accumulate(Either<L, R> either) {
      if (this.left != null) {
        return;
      }
      either.accept(l -> this.left = l,
          this.values::add);
    }

    Acc<L, R> combine(Acc<L, R> other) {
      if (this.left != null) {
        return this;
      }
      if (other.left != null) {
        return other;
      }
      this.values.addAll(other.values);
      return this;
    }

    Either<L, List<R>> finish() {
      if (left != null) {
        return Either.left(left);
      }
      return Either.right(values);
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
