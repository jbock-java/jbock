package net.jbock.coerce;

import org.junit.jupiter.api.Test;

import static net.jbock.coerce.CoercionProvider.snakeToCamel;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CoercionProviderTest {

  @Test
  void testSnakeToCamel() {
    assertNotEquals(snakeToCamel("f_ancy"), snakeToCamel("f__ancy"));
  }
}