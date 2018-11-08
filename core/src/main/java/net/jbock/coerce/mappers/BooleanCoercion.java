package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

final class BooleanCoercion extends CoercionFactory {

  BooleanCoercion() {
    super(Boolean.class);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", Boolean.class));
  }
}
