package net.jbock.coerce.mappers;

import java.math.BigDecimal;

class BigDecimalCoercion extends SimpleCoercion {

  BigDecimalCoercion() {
    super(BigDecimal.class, "new");
  }

}
