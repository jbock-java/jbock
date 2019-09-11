package net.jbock.coerce.mappers;

import java.time.LocalDateTime;

class LocalDateTimeCoercion extends SimpleCoercion {

  LocalDateTimeCoercion() {
    super(LocalDateTime.class, "$T::parse");
  }

}
