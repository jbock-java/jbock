package net.jbock.coerce.mappers;

import java.time.LocalDate;

class LocalDateCoercion extends SimpleCoercion {

  LocalDateCoercion() {
    super(LocalDate.class, "parse");
  }

}
