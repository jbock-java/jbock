package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

class StringCoercion extends CoercionFactory {

  @Override
  CodeBlock mapExpr(TypeMirror innerType) {
    return CodeBlock.of("$T.identity()", Function.class);
  }
}
