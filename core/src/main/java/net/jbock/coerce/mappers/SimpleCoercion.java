package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

class SimpleCoercion extends CoercionFactory {

  private final Class<?> mapperReturnType;

  private final Function<TypeMirror, CodeBlock> mapExpr;

  private SimpleCoercion(Class<?> mapperReturnType, Function<TypeMirror, CodeBlock> mapExpr) {
    this.mapperReturnType = mapperReturnType;
    this.mapExpr = mapExpr;
  }

  static SimpleCoercion create(Class<?> mapperReturnType, String mapExpr) {
    return new SimpleCoercion(mapperReturnType, type -> CodeBlock.of("$T::" + mapExpr, mapperReturnType));
  }

  static SimpleCoercion create(Class<?> mapperReturnType, Function<TypeMirror, CodeBlock> mapExpr) {
    return new SimpleCoercion(mapperReturnType, mapExpr);
  }

  @Override
  final TypeMirror mapperReturnType(TypeTool tool) {
    return tool.getTypeElement(mapperReturnType).asType();
  }

  @Override
  final Optional<CodeBlock> mapExpr(TypeMirror returnType) {
    return Optional.of(mapExpr.apply(returnType));
  }
}
