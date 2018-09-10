package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.math.BigDecimal;

class BigDecimalCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::new)", BigDecimal.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(BigDecimal.class);
  }
}
