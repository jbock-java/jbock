package net.jbock.either;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EitherTest {

  @Test
  void testSingleLeft() {
    Either<String, Integer> hi = Either.left("hi");
    Either<String, List<Integer>> either = Stream.of(hi).collect(Either.toValidList());
    Assertions.assertTrue(either.getLeft().isPresent());
    Assertions.assertEquals("hi", either.getLeft().get());
  }

  @Test
  void testSingleRight() {
    Either<String, List<Integer>> either = Stream.<Either<String, Integer>>of(
        Either.right(5),
        Either.right(6),
        Either.right(7)).collect(Either.toValidList());
    Assertions.assertTrue(either.getRight().isPresent());
    Assertions.assertEquals(List.of(5, 6, 7), either.getRight().get());
  }
}