package net.jbock.either;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EitherTest {

  @Test
  void testSingleLeft() {
    Either<String, Integer> hi = Either.left("hi");
    Either<String, List<Integer>> either = Stream.of(hi).collect(Either.toValidList());
    assertTrue(either.isLeft());
    either.acceptLeft(l -> assertEquals("hi", l));
  }

  @Test
  void testSingleRight() {
    Either<String, List<Integer>> either = Stream.<Either<String, Integer>>of(
        Either.right(5),
        Either.right(6),
        Either.right(7)).collect(Either.toValidList());
    assertTrue(either.isRight());
    either.acceptRight(r -> assertEquals(List.of(5, 6, 7), r));
  }
}