package net.jbock.examples;

import java.math.BigInteger;
import java.util.function.Function;

class CustomBigIntegerMapper implements Function<String, BigInteger> {

  @Override
  public BigInteger apply(String s) {
    if (s.startsWith("0x")) {
      return new BigInteger(s.substring(2), 16);
    }
    return new BigInteger(s);
  }
}
