package net.jbock.examples.fixture;

import net.jbock.either.Either;
import net.jbock.usage.UsageDocumentation;
import net.jbock.util.NotSuccess;
import net.jbock.util.HasMessage;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test util
 */
public final class ParserTestFixture<E> {

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_RED = "\u001B[31m";
  private static final int MAX_LINE_WIDTH = 82;

  private static void printErr(String text) {
    System.out.println(ANSI_RED + text + ANSI_RESET);
  }

  private static void formatErr(String format, Object... args) {
    System.out.print(ANSI_RED + String.format(format, args) + ANSI_RESET);
  }

  public interface Parser<E> {
    Optional<E> parse(String[] args);
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
          Either<NotSuccess, E> parseResult = (Either<NotSuccess, E>)
              parseMethod.invoke(builder, new Object[]{args});
          return parseResult.getRight();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          return Optional.empty();
        }
      }
    });
    return new ParserTestFixture<>(parser.get(0));
  }

  public JsonAssert<E> assertThat(String... args) {
    return new JsonAssert<>(args, parser);
  }

  public E parse(String... args) {
    Optional<E> result = parser.parse(args);
    return result.get();
  }

  public static void assertEquals(String[] actual, String... expected) {
    assertArraysEquals(expected, actual);
  }

  public static void assertArraysEquals(String[] expected, String[] actual) {
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
      Optional<E> result = parser.parse(args);
      return new Parsed(stderr.toString(), result);
    }

    private final String[] args;

    private final Parser<E> parser;

    private JsonAssert(String[] args, Parser<E> parser) {
      this.args = args;
      this.parser = parser;
    }

    public void satisfies(Predicate<E> predicate) {
      Parsed parsed = getParsed();
      assertTrue(parsed.isPresent(), "Parsing was not successful");
      assertTrue(predicate.test(parsed.get()));
    }

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
  }

  public HasMessage castToError(NotSuccess notSuccess) {
    return (HasMessage) notSuccess;
  }

  public String[] getUsageDocumentation(NotSuccess notSuccess) {
    return getUsageDocumentation(notSuccess, Map.of());
  }

  public String[] getUsageDocumentation(
      NotSuccess notSuccess,
      Map<String, String> messages) {
    TestOutputStream testOutputStream = new TestOutputStream();
    UsageDocumentation.builder(notSuccess.commandModel())
        .withErrorStream(testOutputStream.out)
        .withTerminalWidth(MAX_LINE_WIDTH)
        .withMessages(messages)
        .build()
        .printUsageDocumentation();
    return testOutputStream.split();
  }
}
