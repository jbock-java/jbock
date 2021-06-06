package net.jbock.either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

class ValidatingCollector<L, R> implements Collector<Either<L, R>, Either<L, ArrayList<R>>, Either<L, List<R>>> {

  private static final Set<Collector.Characteristics> CH_ID
      = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

  @Override
  public Supplier<Either<L, ArrayList<R>>> supplier() {
    return () -> Either.right(new ArrayList<>());
  }

  @Override
  public BiConsumer<Either<L, ArrayList<R>>, Either<L, R>> accumulator() {
    return (acc, value) -> acc.flatMap(r -> value.map(v -> {
      r.add(v);
      return Either.right(r);
    }));
  }

  @Override
  public BinaryOperator<Either<L, ArrayList<R>>> combiner() {
    return (acc1, acc2) -> acc1.flatMap(r1 -> acc2.map(r2 -> {
      r1.addAll(r2);
      return r1;
    }));
  }

  @Override
  public Function<Either<L, ArrayList<R>>, Either<L, List<R>>> finisher() {
    return castingIdentity();
  }

  @Override
  public Set<Characteristics> characteristics() {
    return CH_ID;
  }

  @SuppressWarnings("unchecked")
  private static <I, R> Function<I, R> castingIdentity() {
    return i -> (R) i;
  }
}
