package net.jbock.examples.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;

public final class JsonFixture {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private JsonFixture() {
  }

  public static JsonNode readJson(Object json) {
    try {
      return MAPPER.readTree(json.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonNode expectedJson(Object... kvs) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
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
        List v1 = (List) v;
        array.addAll(v1);
      }
    }
    return node;
  }
}
