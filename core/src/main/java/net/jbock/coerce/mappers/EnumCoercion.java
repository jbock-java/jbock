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
  final Optional<CodeBlock> mapExpr(TypeMirror returnType) {
    return Optional.of(CodeBlock.of("$T::valueOf", returnType));
  }

  public static EnumCoercion create(TypeMirror returnType) {
    return new EnumCoercion(returnType);
  }

  @Override
  TypeMirror mapperReturnType(TypeTool tool) {
    return mapperReturnType;
  }
}
