package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;

class OptionalLongCoercion extends BasicLongCoercion {

  @Override
  public TypeName trigger() {
    return Constants.OPTIONAL_LONG;
  }

  @Override
  public boolean special() {
    return true;
  }

  @Override
  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($L -> $L)",
        "e", jsonExpr("e")).build();
  }

  @Override
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder()
        .add("($L.isPresent() ? $L.getAsLong() : $S)",
            param,
            param,
            "null")
        .build();
  }
}
