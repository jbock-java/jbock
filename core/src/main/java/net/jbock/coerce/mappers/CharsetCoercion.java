package net.jbock.coerce.mappers;

import java.nio.charset.Charset;

class CharsetCoercion extends SimpleCoercion {

  CharsetCoercion() {
    super(Charset.class, "$T::forName");
  }

}
