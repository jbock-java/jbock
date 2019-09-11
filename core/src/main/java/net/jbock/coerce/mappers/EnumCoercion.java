package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class EnumCoercion extends CoercionFactory {

  private final TypeMirror mapperReturnType;

  private EnumCoercion(TypeMirror mapperReturnType) {
    this.mapperReturnType = mapperReturnType;
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", mapperReturnType));
  }

  public static EnumCoercion create(TypeMirror mapperReturnType) {
    return new EnumCoercion(mapperReturnType);
  }

  @Override
  TypeMirror mapperReturnType(TypeTool tool) {
    return mapperReturnType;
  }
}
