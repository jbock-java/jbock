package net.jbock.examples.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test util
 */
public final class ParserTestFixture<E> {

  public interface Parser<E> {
    Optional<E> parse(String[] args);

    Parser<E> out(PrintStream out);

    Parser<E> indent(int indent);
  }

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final Parser<E> parser;

  private ParserTestFixture(Parser<E> parser) {
    this.parser = parser;
  }

  private static JsonNode readJson(String json) {
    try {
      return MAPPER.readTree(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static JsonNode parseJson(Object... kvs) {
    ObjectNode node = MAPPER.createObjectNode();
    if (kvs.length % 2 != 0) {
      throw new IllegalArgumentException("length must be even: " + Arrays.toString(kvs));
    }
    Set<String> keys = new HashSet<>();
    for (int i = 0; i < kvs.length; i += 2) {
      String k = kvs[i].toString();
      if (!keys.add(k)) {
        throw new IllegalArgumentException("duplicate key: " + k);
      }
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
        ArrayNode array = node.putArray(k);
        for (Object s : list) {
          if (s instanceof Integer) {
            array.add(((Integer) s));
          } else {
            array.add(s.toString());
          }
        }
      }
    }
    return node;
  }

  public static <E> ParserTestFixture<E> create(Object builder) {
    List<Parser<E>> parser = new ArrayList<>(1);
    parser.add(new Parser<E>() {

      @Override
      @SuppressWarnings("unchecked")
      public Optional<E> parse(String[] args) {
        try {
          Method parseMethod = builder.getClass().getDeclaredMethod("parse", args.getClass());
          parseMethod.setAccessible(true);
          return (Optional<E>) parseMethod.invoke(builder, new Object[]{args});
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Parser<E> out(PrintStream out) {
        try {
          Method outMethod = builder.getClass().getDeclaredMethod("out", PrintStream.class);
          outMethod.setAccessible(true);
          outMethod.invoke(builder, out);
          return parser.get(0);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Parser<E> indent(int indent) {
        try {
          Method indentMethod = builder.getClass().getDeclaredMethod("indent", Integer.TYPE);
          indentMethod.setAccessible(true);
          indentMethod.invoke(builder, indent);
          return parser.get(0);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }
    });
    return new ParserTestFixture<>(parser.get(0));
  }

  public JsonAssert<E> assertThat(String... args) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);
    Optional<E> parsed = parser.out(out).indent(2).parse(args);
    return new JsonAssert<>(parsed, new String(baos.toByteArray()));
  }

  public E parse(String... args) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);
    Optional<E> result = parser.out(out).indent(2).parse(args);
    if (!result.isPresent()) {
      throw new AssertionError(new String(baos.toByteArray()));
    }
    return result.get();
  }

  public void assertPrints(String... expected) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Optional<E> result = parser.out(new PrintStream(out)).indent(2).parse(new String[]{"--help"});
    assertFalse(result.isPresent());
    String[] actual = new String(out.toByteArray()).split("\\r?\\n", -1);
    compareArrays(expected, actual);
  }

  private static void compareArrays(String[] expected, String[] actual) {
    if (expected.length != actual.length) {
      System.err.println("Expected:");
      System.err.flush();
      for (int i = 0; i < expected.length; i++) {
        System.err.format("%3d: %s%n", i, expected[i]);
        System.err.flush();
      }
      System.err.println();
      System.err.println("Actual:");
      System.err.flush();
      for (int i = 0; i < actual.length; i++) {
        System.err.format("%3d: %s%n", i, actual[i]);
        System.err.flush();
      }
      fail(String.format("Expected length: %d, actual length: %d", expected.length, actual.length));
    }
    for (int i = 0; i < actual.length; i++) {
      if (!Objects.equals(expected[i], actual[i])) {
        System.err.println("Actual:");
        System.err.flush();
        for (int j = 0; j <= i; j++) {
          System.err.format("%3d: <%s> <%s>%n", j, expected[j], actual[j]);
          System.err.flush();
        }
        fail("Arrays differ at index " + i);
      }
    }
  }

  public static final class JsonAssert<E> {

    private final Optional<E> parsed;

    private final String e;

    private JsonAssert(Optional<E> parsed, String e) {
      this.parsed = parsed;
      this.e = e;
    }

    public void failsWithLine4(String expectedMessage) {
      if (parsed.isPresent()) {
        fail("Expecting a failure" +
            " but parsing was successful");
      }
      assertTrue(e.startsWith("Usage:"));
      String actualMessage = e.split("\\r?\\n", -1)[4].trim();
      assertEquals(expectedMessage, actualMessage);
    }

    public void failsWithLines(String... expected) {
      if (parsed.isPresent()) {
        fail("Expecting a failure" +
            " but parsing was successful");
      }
      String[] actualMessage = e.split("\\r?\\n", -1);
      compareArrays(expected, actualMessage);
    }

    public void satisfies(Predicate<E> predicate) {
      assertTrue(parsed.isPresent(), "Parsing was not successful");
      assertTrue(predicate.test(parsed.get()));
    }

    public void succeeds(Object... expected) {
      assertTrue(parsed.isPresent(), "Parsing was not successful");
      String jsonString = parsed.get().toString();
      JsonNode actualJson = readJson(jsonString);
      JsonNode expectedJson = parseJson(expected);
      assertEquals(expectedJson, actualJson);
    }
  }
}
