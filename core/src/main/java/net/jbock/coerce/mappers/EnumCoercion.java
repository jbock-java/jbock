package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class EnumCoercion extends CoercionFactory {

  private EnumCoercion(TypeMirror mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::valueOf", mapperReturnType()).build());
  }

  public static EnumCoercion create(TypeMirror mapperReturnType) {
    return new EnumCoercion(mapperReturnType);
  }
}
