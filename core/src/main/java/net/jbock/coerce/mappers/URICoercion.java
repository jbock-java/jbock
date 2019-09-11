package net.jbock.coerce.mappers;

import java.net.URI;

class URICoercion extends SimpleCoercion {

  URICoercion() {
    super(URI.class, "$T::create");
  }

}
