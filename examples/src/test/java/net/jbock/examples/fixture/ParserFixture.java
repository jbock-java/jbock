package net.jbock.examples.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;

public final class ParserFixture<E> {

  public interface TriFunction<A, B, D> {
    D apply(A a, B b, int c);
  }

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final TriFunction<String[], PrintStream, Optional<E>> parseMethod;

  private ParserFixture(
      TriFunction<String[], PrintStream, Optional<E>> parseMethod) {
    this.parseMethod = parseMethod;
  }

  private static JsonNode readJson(String json) {
    try {
      return MAPPER.readTree(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static JsonNode parseJson(Object... kvs) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    if (kvs.length % 2 != 0) {
      throw new IllegalArgumentException("length must be even: " + Arrays.toString(kvs));
    }
    for (int i = 0; i < kvs.length; i += 2) {
      String k = kvs[i].toString();
      Object v = kvs[i + 1];
      if (v == null) {
        node.put(k, (String) null);
      }
      if (v instanceof Integer) {
        node.put(k, (Integer) v);
      } else if (v instanceof String) {
        node.put(k, (String) v);
      } else if (v instanceof Boolean) {
        node.put(k, (Boolean) v);
      } else if (v instanceof List) {
        List list = (List) v;
        if (!list.isEmpty()) {
          ArrayNode array = node.putArray(k);
          for (Object s : list) {
            array.add(s.toString());
          }
        }
      }
    }
    return node;
  }

  public static <E> ParserFixture<E> create(
      TriFunction<String[], PrintStream, Optional<E>> fn) {
    return new ParserFixture<>(fn);
  }

  public JsonAssert<E> assertThat(String... args) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);
    Optional<E> parsed = parseMethod.apply(args, out, 2);
    return new JsonAssert<>(parsed, new String(baos.toByteArray()));
  }

  public void assertPrints(String... expected) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Optional<E> result = parseMethod.apply(new String[]{"--help"}, new PrintStream(out), 2);
    Assertions.assertFalse(result.isPresent());
    String[] actual = new String(out.toByteArray()).split("\\r?\\n", -1);
    Assertions.assertArrayEquals(expected, actual, "Actual: " + Arrays.toString(actual));
  }

  public static final class JsonAssert<E> {

    private final Optional<E> parsed;

    private final String e;

    private JsonAssert(Optional<E> parsed, String e) {
      this.parsed = parsed;
      this.e = e;
    }

    public void failsWithLine1(String expectedMessage) {
      if (parsed.isPresent()) {
        Assertions.fail("Expecting a failure" +
            " but parsing was successful");
      }
      Assertions.assertTrue(e.startsWith("Usage:"));
      Assertions.assertTrue(e.contains("\n"));
      String actualMessage = e.split("\\r?\\n", -1)[1];
      Assertions.assertEquals(expectedMessage, actualMessage);
    }

    public void failsWithLines(String... expected) {
      if (parsed.isPresent()) {
        Assertions.fail("Expecting a failure" +
            " but parsing was successful");
      }
      String[] actualMessage = e.split("\\r?\\n", -1);
      Assertions.assertArrayEquals(expected, actualMessage, "Actual: " + Arrays.toString(actualMessage));
    }

    public void satisfies(Predicate<E> predicate) {
      Assertions.assertTrue(parsed.isPresent(), "Parsing was not successful");
      Assertions.assertTrue(predicate.test(parsed.get()));
    }

    public void succeeds(Object... expected) {
      Assertions.assertTrue(parsed.isPresent(), "Parsing was not successful");
      Assertions.assertEquals(parseJson(expected),
          readJson(parsed.get().toString()));
    }
  }
}
