package net.jbock.coerce.mappers;

import java.time.OffsetDateTime;

class OffsetDateTimeCoercion extends SimpleCoercion {

  OffsetDateTimeCoercion() {
    super(OffsetDateTime.class, "$T::parse");
  }

}
