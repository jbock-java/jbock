package net.jbock.coerce;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParameterTypeTest {

  private int booleanToInt(boolean b) {
    return b ? 1 : 0;
  }

  @Test
  void testBooleans() {
    Set<List<Integer>> masks = new HashSet<>();
    for (ParameterStyle type : ParameterStyle.values()) {
      masks.add(Stream.of(
          type.isFlag(),
          type.isOptional(),
          type.isRepeatable(),
          type.isRequired())
          .map(this::booleanToInt)
          .collect(Collectors.toList()));
    }
    assertEquals(ParameterStyle.values().length, masks.size());
    for (List<Integer> mask : masks) {
      assertEquals(1, mask.stream().mapToInt(i -> i).sum());
    }
  }
}
