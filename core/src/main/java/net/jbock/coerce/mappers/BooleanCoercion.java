package net.jbock.coerce.mappers;

final class BooleanCoercion extends SimpleCoercion {

  BooleanCoercion() {
    super(Boolean.class, "$T::valueOf");
  }

}
