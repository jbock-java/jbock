package net.jbock.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface Either<L, R> {

    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    <U> U fold(
            Function<? super L, ? extends U> leftMapper,
            Function<? super R, ? extends U> rightMapper);

    default <X extends RuntimeException> R orElseThrow(
            Function<? super L, ? extends X> exceptionSupplier) throws X {
        return fold(l -> {
            throw exceptionSupplier.apply(l);
        }, Function.identity());
    }

    record Left<L, R>(L value) implements Either<L, R> {

        @Override
        public <U> U fold(
                Function<? super L, ? extends U> leftMapper,
                Function<? super R, ? extends U> rightMapper) {
            return leftMapper.apply(value);
        }
    }

    record Right<L, R>(R value) implements Either<L, R> {

        @Override
        public <U> U fold(
                Function<? super L, ? extends U> leftMapper,
                Function<? super R, ? extends U> rightMapper) {
            return rightMapper.apply(value);
        }
    }

    static <L, R>
    Collector<Either<L, R>, ?, Either<L, List<R>>>
    firstFailure() {

        BiConsumer<Acc<L, R>, Either<L, R>> accumulator = (acc, either) ->
                either.fold(acc::addLeft, acc::addRight);

        BinaryOperator<Acc<L, R>> combiner = Acc::combine;

        return new CollectorImpl<>(Acc::new, accumulator, combiner, Acc::finish);
    }

    record CollectorImpl<T, A, R>(
            Supplier<A> supplier,
            BiConsumer<A, T> accumulator,
            BinaryOperator<A> combiner,
            Function<A, R> finisher) implements Collector<T, A, R> {

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }
    }

    final class Acc<L, R> {
        L left;
        private ArrayList<R> right;

        void combineLeft(L otherLeft) {
            addLeft(otherLeft);
        }

        // nullable
        L leftColl() {
            return left;
        }

        L addLeft(L value) {
            if (left == null) {
                left = value;
            }
            return value;
        }

        R addRight(R value) {
            if (leftColl() != null) {
                return value;
            }
            if (right == null) {
                right = new ArrayList<>();
            }
            right.add(value);
            return value;
        }

        Acc<L, R> combine(Acc<L, R> other) {
            if (leftColl() != null) {
                combineLeft(other.leftColl());
                return this;
            }
            if (other.leftColl() != null) {
                return other;
            }
            if (other.right == null) {
                return this;
            }
            if (right == null) {
                right = other.right;
            } else {
                right.addAll(other.right);
            }
            return this;
        }

        Either<L, List<R>> finish() {
            L left = leftColl();
            return left != null
                    ? left(left)
                    : right(right == null ? List.of() : right);
        }
    }
}
