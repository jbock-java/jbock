package net.jbock.coerce.mappers;

final class LongCoercion extends SimpleCoercion {

  LongCoercion() {
    super(Long.class, "$T::valueOf");
  }
  
}
