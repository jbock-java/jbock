package net.jbock.examples.fixture;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    Object parseOrExit(String[] args);

    Parser<E> withErrorStream(PrintStream out);

    Parser<E> withMessages(Map<String, String> map);

    Parser<E> withTerminalWidth(int chars);

    Parser<E> withExitHook(Consumer<?> exitHook);
  }

  private final Parser<E> parser;

  private ParserTestFixture(Parser<E> parser) {
    this.parser = parser;
  }

  public static <E> ParserTestFixture<E> create(Object builder) {
    List<Parser<E>> parser = new ArrayList<>(1);
    parser.add(new Parser<E>() {

      private Parser<E> callSetter(String methodName, Object parameter) {
        return callSetter(methodName, parameter, parameter.getClass());
      }

      private Parser<E> callSetter(String methodName, Object parameter, Class<?> parameterType) {
        try {
          Method outMethod = builder.getClass().getDeclaredMethod(methodName, parameterType);
          outMethod.setAccessible(true);
          outMethod.invoke(builder, parameter);
          return parser.get(0);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public Optional<E> parse(String[] args) {
        try {
          Method parseMethod = builder.getClass().getDeclaredMethod("parse", args.getClass());
          parseMethod.setAccessible(true);
          Object parseResult = parseMethod.invoke(builder, new Object[]{args});
          Method resultMethod = parseResult.getClass().getDeclaredMethod("getResult");
          resultMethod.setAccessible(true);
          return Optional.of((E) resultMethod.invoke(parseResult));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
          return Optional.empty();
        }
      }

      @Override
      public Object parseOrExit(String[] args) {
        try {
          Method outMethod = builder.getClass().getDeclaredMethod("parseOrExit", args.getClass());
          outMethod.setAccessible(true);
          return outMethod.invoke(builder, new Object[]{args});
        } catch (IllegalAccessException | NoSuchMethodException e) {
          return Optional.empty();
        } catch (InvocationTargetException e) {
          throw (RuntimeException) e.getCause();
        }
      }

      @Override
      public Parser<E> withErrorStream(PrintStream err) {
        return callSetter("withErrorStream", err);
      }

      @Override
      public Parser<E> withMessages(Map<String, String> map) {
        return callSetter("withMessages", map, Map.class);
      }

      @Override
      public Parser<E> withTerminalWidth(int width) {
        return callSetter("withTerminalWidth", width, Integer.TYPE);
      }

      @Override
      public Parser<E> withExitHook(Consumer<?> exitHook) {
        return callSetter("withExitHook", exitHook, Consumer.class);
      }
    });
    return new ParserTestFixture<>(parser.get(0));
  }

  public JsonAssert<E> assertThat(String... args) {
    return new JsonAssert<>(args, parser);
  }

  public E parse(String... args) {
    TestOutputStream stderr = new TestOutputStream();
    Optional<E> result = parser
        .withErrorStream(stderr.out).withTerminalWidth(MAX_LINE_WIDTH).parse(args);
    if (!result.isPresent()) {
      throw new AssertionError(stderr.toString());
    }
    return result.get();
  }

  public void assertPrintsHelp(String... expected) {
    String[] actual = getOut();
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
      Optional<E> result = parser
          .withErrorStream(stderr.out).withTerminalWidth(MAX_LINE_WIDTH).parse(args);
      return new Parsed(stderr.toString(), result);
    }

    private final String[] args;

    private final Parser<E> parser;

    private JsonAssert(String[] args, Parser<E> parser) {
      this.args = args;
      this.parser = parser;
    }

    public void failsWithMessage(String expectedMessage) {
      String stderr = getErr();
      if (stderr.isEmpty()) {
        fail("no output on stderr");
      }
      String[] tokens = stderr.split("\\R", -1);
      for (String token : tokens) {
        String prefix = "ERROR: ";
        if (token.startsWith(prefix)) {
          if (!expectedMessage.equals(token.substring(prefix.length()))) {
            Arrays.stream(tokens).forEach(System.err::println);
            fail();
          }
          return;
        }
        String prefixAnsi = "\u001B[31;1mERROR\u001B[m ";
        if (token.startsWith(prefixAnsi)) {
          if (!expectedMessage.equals(token.substring(prefixAnsi.length()))) {
            Arrays.stream(tokens).forEach(System.err::println);
            fail();
          }
          return;
        }
      }
      fail("Error line not found");
    }

    private String getErr() {
      TestOutputStream stderr = new TestOutputStream();
      try {
        parser.withErrorStream(stderr.out)
            .withExitHook(r -> {
              throw new Abort();
            })
            .withTerminalWidth(MAX_LINE_WIDTH)
            .parseOrExit(args);
        fail("Expecting empty result");
      } catch (Abort e) {
        // noop
      }
      return stderr.toString();
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
          assertEquals(expectedValue, result,
              String.format("At `%s`: expecting %s but found %s", key, expectedValue, result));
        }
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        fail(e);
      }
    }
  }

  public String[] getHelp(Map<String, String> bundle) {
    parser.withMessages(bundle);
    return getOut();
  }

  private String[] getOut() {
    TestOutputStream stderr = new TestOutputStream();
    try {
      parser.withErrorStream(stderr.out)
          .withExitHook(r -> {
            throw new Abort();
          })
          .withTerminalWidth(MAX_LINE_WIDTH)
          .parseOrExit(new String[]{"--help"});
      fail("Expecting empty result");
    } catch (Abort e) {
      // noop
    }
    return stderr.toString().split("\\R", -1);
  }

  private static class Abort extends RuntimeException {

  }
}
