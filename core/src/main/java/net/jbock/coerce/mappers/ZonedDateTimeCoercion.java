package net.jbock.coerce.mappers;

import java.time.ZonedDateTime;

class ZonedDateTimeCoercion extends SimpleCoercion {

  ZonedDateTimeCoercion() {
    super(ZonedDateTime.class, "$T::parse");
  }

}
