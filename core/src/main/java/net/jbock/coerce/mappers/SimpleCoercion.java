package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

class SimpleCoercion extends CoercionFactory {

  private final Class<?> mapperReturnType;

  private final CodeBlock mapExpr;

  SimpleCoercion(Class<?> mapperReturnType, String mapExpr) {
    this(mapperReturnType, CodeBlock.of("$T::" + mapExpr, mapperReturnType));
  }

  SimpleCoercion(Class<?> mapperReturnType, CodeBlock mapExpr) {
    this.mapperReturnType = mapperReturnType;
    this.mapExpr = mapExpr;
  }

  static SimpleCoercion create(Class<?> mapperReturnType, String mapExpr) {
    return new SimpleCoercion(mapperReturnType, CodeBlock.of("$T::" + mapExpr, mapperReturnType));
  }

  static SimpleCoercion create(Class<?> mapperReturnType, CodeBlock mapExpr) {
    return new SimpleCoercion(mapperReturnType, mapExpr);
  }

  @Override
  final TypeMirror mapperReturnType(TypeTool tool) {
    return tool.getTypeElement(mapperReturnType).asType();
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(mapExpr);
  }
}
