package net.jbock.coerce.mappers;

import java.math.BigInteger;

class BigIntegerCoercion extends SimpleCoercion {

  BigIntegerCoercion() {
    super(BigInteger.class, "new");
  }

}
