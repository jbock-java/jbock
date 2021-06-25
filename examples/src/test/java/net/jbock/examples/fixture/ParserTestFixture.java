package net.jbock.examples.fixture;

import net.jbock.contrib.StandardErrorHandler;
import net.jbock.either.Either;
import net.jbock.util.HasMessage;
import net.jbock.util.NotSuccess;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test util
 */
public final class ParserTestFixture<E> {

  private static final String ANSI_RED = "\u001B[31m";
  private static final String ANSI_RESET = "\u001B[m";
  private static final int MAX_LINE_WIDTH = 82;

  private static void printErr(String text) {
    System.out.println(ANSI_RED + text + ANSI_RESET);
  }

  private static void formatErr(String format, Object... args) {
    System.out.print(ANSI_RED + String.format(format, args) + ANSI_RESET);
  }

  private final Function<String[], Either<NotSuccess, E>> parser;

  private ParserTestFixture(Function<String[], Either<NotSuccess, E>> parser) {
    this.parser = parser;
  }

  public static <E> ParserTestFixture<E> create(Function<String[], Either<NotSuccess, E>> parser) {
    return new ParserTestFixture<>(parser);
  }

  public JsonAssert<E> assertThat(String... args) {
    return new JsonAssert<>(args, parser);
  }

  public void assertPrintsHelp(Map<String, String> messages, String... expected) {
    String[] input = {"--help"};
    Either<NotSuccess, E> result = parser.apply(input);
    Assertions.assertTrue(result.getLeft().isPresent());
    String[] actual = getUsageDocumentation(result.getLeft().get(), messages);
    assertEquals(expected, actual);
  }

  public void assertPrintsHelp(String... expected) {
    String[] input = {"--help"};
    Either<NotSuccess, E> result = parser.apply(input);
    Assertions.assertTrue(result.getLeft().isPresent());
    String[] actual = getUsageDocumentation(result.getLeft().get());
    assertEquals(expected, actual);
  }

  public E parse(String... args) {
    return parser.apply(args)
        .orElseThrow(l -> new RuntimeException("expecting success but found " + l.getClass()));
  }

  static void assertEquals(String[] actual, String... expected) {
    assertArraysEquals(expected, actual);
  }

  static void assertArraysEquals(String[] expected, String[] actual) {
    if (expected.length != actual.length) {
      failDifferentLength(expected, actual);
    }
    int diffIndex = findIndexFirstDifference(expected, actual);
    if (diffIndex < 0) {
      return;
    }
    for (int j = 0; j < actual.length; j++) {
      System.out.format("%3d: %s%n", j, expected[j]);
      if (!Objects.equals(expected[j], actual[j])) {
        formatErr("%3d: %s%n", j, actual[j]);
      }
    }
    fail("Arrays differ at index " + diffIndex);
  }

  private static int findIndexFirstDifference(String[] expected, String[] actual) {
    int failIndex = -1;
    for (int i = 0; i < actual.length; i++) {
      if (!Objects.equals(expected[i], actual[i])) {
        failIndex = i;
        break;
      }
    }
    return failIndex;
  }

  private static void failDifferentLength(String[] expected, String[] actual) {
    printErr("Expected:");
    for (int i = 0; i < expected.length; i++) {
      formatErr("%3d: %s%n", i, expected[i]);
    }
    System.out.println();
    printErr("Actual:");
    for (int i = 0; i < actual.length; i++) {
      formatErr("%3d: %s%n", i, actual[i]);
    }
    fail(String.format("Expected length: %d, actual length: %d", expected.length, actual.length));
  }

  public static final class JsonAssert<E> {

    class Parsed {
      final String stderr;
      final Optional<E> result;

      Parsed(String stderr, Optional<E> result) {
        this.stderr = stderr;
        this.result = result;
      }

      boolean isPresent() {
        return result.isPresent();
      }

      E get() {
        return result.get();
      }
    }

    Parsed getParsed() {
      TestOutputStream stderr = new TestOutputStream();
      Optional<E> result = parser.apply(args).getRight();
      return new Parsed(stderr.toString(), result);
    }

    private final String[] args;

    private final Function<String[], Either<NotSuccess, E>> parser;

    private JsonAssert(String[] args, Function<String[], Either<NotSuccess, E>> parser) {
      this.args = args;
      this.parser = parser;
    }

    public void satisfies(Predicate<E> predicate) {
      Parsed parsed = getParsed();
      assertTrue(parsed.isPresent(), "Parsing was not successful");
      assertTrue(predicate.test(parsed.get()));
    }

    /**
     * Assert that the parsing result matches the expected output.
     *
     * @param expected key-value pairs
     */
    public void succeeds(Object... expected) {
      Parsed parsed = getParsed();
      try {
        assertTrue(parsed.isPresent(), "Parsing was not successful: " + parsed.stderr);
        assertTrue(parsed.stderr.isEmpty());
        for (int i = 0; i < expected.length; i += 2) {
          String key = (String) expected[i];
          Object expectedValue = expected[i + 1];
          Method method = parsed.get().getClass().getDeclaredMethod(key);
          method.setAccessible(true);
          Object result = method.invoke(parsed.get());
          Assertions.assertEquals(expectedValue, result,
              String.format("At `%s`: expecting %s but found %s", key, expectedValue, result));
        }
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        fail(e);
      }
    }

    public void fails(String m) {
      fails(m::equals);
    }

    private void fails(Predicate<String> messageTest) {
      Optional<HasMessage> hasMessage = parser.apply(args).getLeft()
          .map(notSuccess -> (HasMessage) notSuccess);
      Assertions.assertTrue(hasMessage.isPresent());
      boolean success = messageTest.test(hasMessage.get().message());
      if (!success) {
        Assertions.fail("Assertion failed, message: " + hasMessage.get().message());
      }
    }
  }

  private String[] getUsageDocumentation(NotSuccess notSuccess) {
    return getUsageDocumentation(notSuccess, Map.of());
  }

  private String[] getUsageDocumentation(
      NotSuccess notSuccess,
      Map<String, String> messages) {
    TestOutputStream testOutputStream = new TestOutputStream();
    StandardErrorHandler.builder()
        .withOutputStream(testOutputStream.out)
        .withTerminalWidth(MAX_LINE_WIDTH)
        .withMessages(messages)
        .build()
        .handle(notSuccess);
    return testOutputStream.split();
  }
}
