package net.jbock.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class Constants {

  static final Set<Modifier> ALLOWED_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

  public static final ClassName STRING = ClassName.get(String.class);

  public static final TypeName LIST_OF_STRING = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  public static final TypeName STRING_TO_STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);

  public static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);

  public static final TypeName ENTRY_STRING_STRING =
      ParameterizedTypeName.get(ClassName.get(Map.Entry.class), STRING, STRING);

  public static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(
      ClassName.get(Iterator.class), STRING);

  public static TypeName listOf(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(List.class), typeName);
  }
}
