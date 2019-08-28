package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomCollectorArgumentsTest {

  private ParserTestFixture<CustomCollectorArguments> f =
      ParserTestFixture.create(CustomCollectorArguments_Parser.create());

  @Test
  void testNoMapper() {
    CustomCollectorArguments parsed = f.parse(
        "-H", "A",
        "-H", "A",
        "-H", "HA");
    assertEquals(new HashSet<>(asList("A", "HA")), parsed.strings());
  }

  @Test
  void testBuiltinMapper() {
    CustomCollectorArguments parsed = f.parse(
        "-B", "1",
        "-B", "1",
        "-B", "2");
    assertEquals(new HashSet<>(asList(1, 2)), parsed.integers());
  }

  @Test
  void testCustomMapper() {
    CustomCollectorArguments parsed = f.parse(
        "-M", "0x5",
        "-M", "0xA",
        "-M", "10");
    assertEquals(Stream.of(5L, 10L).map(BigInteger::valueOf).collect(toSet()), parsed.bigIntegers());
  }

  @Test
  void testCustomEnum() {
    CustomCollectorArguments parsed = f.parse(
        "-K", "PEANUTS",
        "-K", "BIG_TIME");
    assertEquals(Stream.of(
        CustomCollectorArguments.Money.PEANUTS,
        CustomCollectorArguments.Money.BIG_TIME).collect(toSet()),
        parsed.moneySet());
  }

  @Test
  void testMap() {
    CustomCollectorArguments parsed = f.parse(
        "-T", "A:2004-11-11",
        "-T", "B:2018-11-12");
    assertEquals(LocalDate.parse("2004-11-11"), parsed.dateMap().get("A"));
    assertEquals(LocalDate.parse("2018-11-12"), parsed.dateMap().get("B"));
  }
}
