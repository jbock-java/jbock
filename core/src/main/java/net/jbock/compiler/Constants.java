package net.jbock.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class Constants {

  public static final ClassName STRING = ClassName.get(String.class);

  public static final TypeName LIST_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  public static final TypeName STREAM_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(Stream.class), STRING);

  public static final TypeName OPTIONAL_STRING = ParameterizedTypeName.get(
      ClassName.get(Optional.class), STRING);

  public static final TypeName STRING_TO_STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);

  public static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);

  public static final TypeName ITERATOR_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(Iterator.class), STRING);
}
