package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.time.OffsetDateTime;

class OffsetDateTimeCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", OffsetDateTime.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(OffsetDateTime.class);
  }

  @Override
  public boolean special() {
    return false;
  }
}
