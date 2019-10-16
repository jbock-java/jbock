package net.jbock.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class Constants {

  public static final ClassName STRING = ClassName.get(String.class);

  public static final ParameterizedTypeName LIST_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  public static final ParameterizedTypeName STREAM_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(Stream.class), STRING);

  static final ParameterizedTypeName STRING_STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);

  static final ArrayTypeName STRING_ARRAY = ArrayTypeName.of(STRING);

  public static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
}
