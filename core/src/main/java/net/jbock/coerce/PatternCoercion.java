package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.regex.Pattern;

class PatternCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::compile)", Pattern.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(Pattern.class);
  }
}
