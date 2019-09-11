package net.jbock.coerce.mappers;

final class IntegerCoercion extends SimpleCoercion {

  IntegerCoercion() {
    super(Integer.class, "$T::valueOf");
  }

}
