package net.jbock.coerce.mappers;

final class FloatCoercion extends SimpleCoercion {

  FloatCoercion() {
    super(Float.class, "valueOf");
  }

}
