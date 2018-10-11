package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.time.LocalDate;

class LocalDateCoercion extends CoercionFactory {

  LocalDateCoercion() {
    super(LocalDate.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", LocalDate.class).build();
  }
}
