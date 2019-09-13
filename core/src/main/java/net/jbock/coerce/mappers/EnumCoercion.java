package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class EnumCoercion extends CoercionFactory {

  @Override
  final Optional<CodeBlock> mapExpr(TypeMirror returnType) {
    return Optional.of(CodeBlock.of("$T::valueOf", returnType));
  }

  public static EnumCoercion create() {
    return new EnumCoercion();
  }
}
