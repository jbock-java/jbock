package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

class StringCoercion extends CoercionFactory {

  @Override
  public CodeBlock createMapper(TypeMirror innerType) {
    return CodeBlock.of("$T.identity()", Function.class);
  }
}
