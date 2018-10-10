package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Iterator;
import java.util.List;

public final class Constants {

  public static final ClassName STRING = ClassName.get(String.class);

  static final ClassName CHARACTER = ClassName.get(Character.class);

  static final ParameterizedTypeName LIST_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  static final ArrayTypeName STRING_ARRAY = ArrayTypeName.of(STRING);

  static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
}
