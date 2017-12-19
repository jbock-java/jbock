package net.jbock.examples.fixture;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Assert;

public final class ParserFixture<E> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final Function<String[], E> parse;

  private ParserFixture(Function<String[], E> parse) {
    this.parse = parse;
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

  public static <E> ParserFixture<E> create(Function<String[], E> fn) {
    return new ParserFixture<>(fn);
  }

  public JsonAssert<E> assertThat(String... args) {
    try {
      E parsed = parse.apply(args);
      return new JsonAssert<>(parsed, null);
    } catch (RuntimeException e) {
      return new JsonAssert<>(null, e);
    }
  }

  public static final class JsonAssert<E> {

    private final E parsed;

    private final RuntimeException e;

    private JsonAssert(E parsed, RuntimeException e) {
      this.parsed = parsed;
      this.e = e;
    }

    public <X extends RuntimeException> void throwsException(
        Class<X> expectedException,
        String expectedMessage) {
      if (e == null) {
        Assert.fail("Expected " + expectedException.getSimpleName() +
            " but no exception was thrown");
      }
      Assert.assertThat(e, is(instanceOf(expectedException)));
      Assert.assertThat(e.getMessage(), is(expectedMessage));
    }

    public void isInvalid(String expectedMessage) {
      throwsException(IllegalArgumentException.class, expectedMessage);
    }

    public void satisfies(Predicate<E> predicate) {
      if (e != null) {
        throw e;
      }
      Assert.assertTrue(predicate.test(parsed));
    }

    public void isParsedAs(Object... expected) {
      if (e != null) {
        throw e;
      }
      Assert.assertThat(readJson(parsed.toString()), is(parseJson(expected)));
    }
  }
}
