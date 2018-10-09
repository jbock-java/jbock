package net.jbock.coerce.mappers;

import javax.lang.model.type.PrimitiveType;

abstract class BasicNumberCoercion extends CoercionFactory {

  BasicNumberCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicNumberCoercion(PrimitiveType trigger) {
    super(trigger);
  }
}
