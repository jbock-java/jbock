package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;

import javax.lang.model.type.PrimitiveType;
import java.util.Objects;

abstract class BasicNumberCoercion extends CoercionFactory {

  BasicNumberCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicNumberCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("$L", param).build();
  }

  CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($T::toString)", Objects.class).build();
  }
}
