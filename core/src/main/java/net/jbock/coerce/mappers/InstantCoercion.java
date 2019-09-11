package net.jbock.coerce.mappers;

import java.time.Instant;

class InstantCoercion extends SimpleCoercion {

  InstantCoercion() {
    super(Instant.class, "$T::parse");
  }

}
