package net.jbock.examples.fixture;

import org.junit.jupiter.api.Assertions;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    Parser<E> withOutputStream(PrintStream out);

    Parser<E> withErrorStream(PrintStream out);

    Parser<E> withIndent(int indent);
  }

  private final Parser<E> parser;

  private ParserTestFixture(Parser<E> parser) {
    this.parser = parser;
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
          Object parseResult = parseMethod.invoke(builder, new Object[]{args});
          Method resultMethod = parseResult.getClass().getDeclaredMethod("result");
          resultMethod.setAccessible(true);
          return (Optional<E>) resultMethod.invoke(parseResult);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Parser<E> withOutputStream(PrintStream out) {
        try {
          Method outMethod = builder.getClass().getDeclaredMethod("withOutputStream", PrintStream.class);
          outMethod.setAccessible(true);
          outMethod.invoke(builder, out);
          return parser.get(0);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Parser<E> withErrorStream(PrintStream out) {
        try {
          Method outMethod = builder.getClass().getDeclaredMethod("withErrorStream", PrintStream.class);
          outMethod.setAccessible(true);
          outMethod.invoke(builder, out);
          return parser.get(0);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Parser<E> withIndent(int indent) {
        try {
          Method indentMethod = builder.getClass().getDeclaredMethod("withIndent", Integer.TYPE);
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
    TestOutputStream stdout = new TestOutputStream();
    TestOutputStream stderr = new TestOutputStream();
    Optional<E> parsed = parser.withOutputStream(stdout.out)
        .withErrorStream(stderr.out).withIndent(2).parse(args);
    return new JsonAssert<>(parsed, stdout.toString(), stderr.toString());
  }

  public E parse(String... args) {
    TestOutputStream stdout = new TestOutputStream();
    TestOutputStream stderr = new TestOutputStream();
    Optional<E> result = parser.withOutputStream(stdout.out)
        .withErrorStream(stderr.out).withIndent(2).parse(args);
    if (!result.isPresent()) {
      throw new AssertionError(stderr.toString());
    }
    return result.get();
  }

  public void assertPrints(String... expected) {
    TestOutputStream stdout = new TestOutputStream();
    TestOutputStream stderr = new TestOutputStream();
    Optional<E> result = parser.withOutputStream(stdout.out)
        .withErrorStream(stderr.out).withIndent(2).parse(new String[]{"--help"});
    assertFalse(result.isPresent());
    String[] actual = stdout.toString().split("\\r?\\n", -1);
    compareArrays(expected, actual);
  }

  private static void compareArrays(String[] expected, String[] actual) {
    if (expected.length != actual.length) {
      System.err.println("Expected:");
      for (int i = 0; i < expected.length; i++) {
        System.err.format("%3d: %s%n", i, expected[i]);
      }
      System.err.println();
      System.err.println("Actual:");
      for (int i = 0; i < actual.length; i++) {
        System.err.format("%3d: %s%n", i, actual[i]);
      }
      System.err.flush();
      fail(String.format("Expected length: %d, actual length: %d", expected.length, actual.length));
    }
    for (int i = 0; i < actual.length; i++) {
      if (!Objects.equals(expected[i], actual[i])) {
        System.err.println("Actual:");
        for (int j = 0; j < i; j++) {
          System.err.format("%3d: <%s>%n", j, expected[j]);
        }
        System.err.format("%3d: <%s> != <%s>%n", i, expected[i], actual[i]);
        System.err.flush();
        fail("Arrays differ at index " + i);
      }
    }
  }

  public static final class JsonAssert<E> {

    private final Optional<E> parsed;

    private final String stdout;

    private final String stderr;

    private JsonAssert(Optional<E> parsed, String stdout, String stderr) {
      this.parsed = parsed;
      this.stdout = stdout;
      this.stderr = stderr;
    }

    public void failsWithLine4(String expectedMessage) {
      if (parsed.isPresent()) {
        fail("Expecting a failure" +
            " but parsing was successful");
      }
      assertTrue(stdout.isEmpty());
      assertTrue(stderr.startsWith("Usage:"));
      String actualMessage = stderr.split("\\r?\\n", -1)[4].trim();
      assertEquals(expectedMessage, actualMessage);
    }

    public void failsWithLines(String... expected) {
      if (parsed.isPresent()) {
        fail("Expecting a failure" +
            " but parsing was successful");
      }
      assertTrue(stdout.isEmpty());
      String[] actualMessage = stderr.split("\\r?\\n", -1);
      compareArrays(expected, actualMessage);
    }

    public void satisfies(Predicate<E> predicate) {
      assertTrue(parsed.isPresent(), "Parsing was not successful");
      assertTrue(predicate.test(parsed.get()));
    }

    public void succeeds(Object... expected) {
      try {
        assertTrue(parsed.isPresent(), "Parsing was not successful: " + stderr);
        assertTrue(stdout.isEmpty());
        assertTrue(stderr.isEmpty());
        E parsed = this.parsed.get();
        for (int i = 0; i < expected.length; i += 2) {
          String key = (String) expected[i];
          Object expectedValue = expected[i + 1];
          Method method = parsed.getClass().getDeclaredMethod(key);
          method.setAccessible(true);
          Object result = method.invoke(parsed);
          assertEquals(expectedValue, result,
              String.format("At `%s`: expecting %s but found %s", key, expectedValue, result));
        }
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        Assertions.fail(e);
      }
    }
  }

}
