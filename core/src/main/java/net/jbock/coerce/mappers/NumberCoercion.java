package net.jbock.coerce.mappers;

abstract class NumberCoercion extends CoercionFactory {

  NumberCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }
}
