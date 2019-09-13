package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

class StringCoercion extends CoercionFactory {

  @Override
  Optional<CodeBlock> mapExpr(TypeMirror returnType) {
    return Optional.empty();
  }
}
