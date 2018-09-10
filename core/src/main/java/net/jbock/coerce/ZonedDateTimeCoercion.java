package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.time.ZonedDateTime;

class ZonedDateTimeCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", ZonedDateTime.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(ZonedDateTime.class);
  }
}
