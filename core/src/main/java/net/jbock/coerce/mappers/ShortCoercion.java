package net.jbock.coerce.mappers;

final class ShortCoercion extends SimpleCoercion {

  ShortCoercion() {
    super(Short.class, "valueOf");
  }

}
