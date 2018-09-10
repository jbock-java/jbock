package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.Constants;

abstract class BasicCharacterCoercion extends Coercion {

  @Override
  public final CodeBlock map() {
    ParameterSpec s = ParameterSpec.builder(Constants.STRING, "s").build();
    return CodeBlock.builder().add(".map(Helper::parseCharacter)").build();
  }
}
