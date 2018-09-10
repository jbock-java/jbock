package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.time.LocalDate;

class LocalDateCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", LocalDate.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(LocalDate.class);
  }
}
