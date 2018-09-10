package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class Constants {

  public static final String JAVA_LANG_STRING = String.class.getCanonicalName();
  public static final String JAVA_UTIL_OPTIONAL_INT = OptionalInt.class.getCanonicalName();

  public static final ClassName STRING = ClassName.get(String.class);
  public static final ClassName INTEGER = ClassName.get(Integer.class);

  public static final ParameterizedTypeName LIST_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  public static final ArrayTypeName STRING_ARRAY = ArrayTypeName.of(STRING);

  public static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);

  public static final TypeName OPTIONAL_INT = TypeName.get(OptionalInt.class);
  public static final TypeName OPTIONAL_LONG = TypeName.get(OptionalLong.class);
  public static final TypeName OPTIONAL_DOUBLE = TypeName.get(OptionalDouble.class);
}
