package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.time.ZonedDateTime;

class ZonedDateTimeCoercion extends CoercionFactory {

  ZonedDateTimeCoercion() {
    super(ZonedDateTime.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", ZonedDateTime.class).build();
  }
}
