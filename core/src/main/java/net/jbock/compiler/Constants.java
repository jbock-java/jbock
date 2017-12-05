package net.jbock.compiler;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

final class Constants {

  static final String JAVA_LANG_STRING = String.class.getCanonicalName();
  static final String JAVA_UTIL_OPTIONAL_INT = OptionalInt.class.getCanonicalName();

  static final ClassName STRING = ClassName.get(String.class);

  static final ParameterizedTypeName OPTIONAL_STRING =
      ParameterizedTypeName.get(ClassName.get(Optional.class), STRING);

  static final ParameterizedTypeName LIST_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
}
