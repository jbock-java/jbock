package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

class SimpleCoercion extends CoercionFactory {

  private final Function<TypeMirror, CodeBlock> mapExpr;

  private SimpleCoercion(Function<TypeMirror, CodeBlock> mapExpr) {
    this.mapExpr = mapExpr;
  }

  static SimpleCoercion create(String mapExpr) {
    return new SimpleCoercion(type -> CodeBlock.of("$T::" + mapExpr, type));
  }

  static SimpleCoercion create(Function<TypeMirror, CodeBlock> mapExpr) {
    return new SimpleCoercion(mapExpr);
  }

  @Override
  final Optional<CodeBlock> mapExpr(TypeMirror returnType) {
    return Optional.of(mapExpr.apply(returnType));
  }
}
