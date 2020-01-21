package net.jbock.coerce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SkewTest {

  @Test
  void testSubset() {
    for (NonFlagSkew skew : NonFlagSkew.values()) {
      Assertions.assertNotNull(Skew.valueOf(skew.name()));
    }
  }
}