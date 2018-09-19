package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.util.regex.Pattern;

class PatternCoercion extends Coercion {

  PatternCoercion() {
    super(Pattern.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::compile)", Pattern.class).build();
  }
}
