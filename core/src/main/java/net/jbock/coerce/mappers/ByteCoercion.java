package net.jbock.coerce.mappers;

final class ByteCoercion extends SimpleCoercion {

  ByteCoercion() {
    super(Byte.class, "$T::valueOf");
  }

}
