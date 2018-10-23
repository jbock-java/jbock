package net.jbock.coerce.mappers;

import javax.lang.model.type.PrimitiveType;

abstract class BasicNumberCoercion extends CoercionFactory {

  BasicNumberCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicNumberCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }
}
