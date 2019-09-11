package net.jbock.coerce.mappers;

final class DoubleCoercion extends SimpleCoercion {

  DoubleCoercion() {
    super(Double.class, "$T::valueOf");
  }

}
