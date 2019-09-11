package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

class StringCoercion extends CoercionFactory {

  @Override
  TypeMirror mapperReturnType(TypeTool tool) {
    return tool.asType(String.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.empty();
  }
}
