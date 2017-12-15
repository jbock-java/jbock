package net.jbock.examples.fixture;

import static org.hamcrest.CoreMatchers.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.Assert;

public final class JsonFixture<E> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final Function<String[], E> parse;

  private JsonFixture(Function<String[], E> parse) {
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
        ArrayNode array = node.putArray(k);
        List<String> v1 = (List) v;
        for (String s : v1) {
          array.add(s);
        }
      }
    }
    return node;
  }

  public static <E> JsonFixture<E> create(Function<String[], E> fn) {
    return new JsonFixture(fn);
  }

  public JsonAssert assertThat(String... args) {
    return new JsonAssert(readJson(parse.apply(args).toString()));
  }

  public static final class JsonAssert {
    private final JsonNode actual;

    private JsonAssert(JsonNode actual) {
      this.actual = actual;
    }

    public void isParsedAs(Object... expected) {
      Assert.assertThat(actual, is(parseJson(expected)));
    }
  }
}
