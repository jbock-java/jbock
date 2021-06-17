package net.jbock.common;

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

  public static final Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

  public static final ClassName STRING = ClassName.get(String.class);

  public static final TypeName LIST_OF_STRING = ParameterizedTypeName.get(ClassName.get(List.class), STRING);

  public static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);

  public static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);

  public static TypeName mapOf(TypeName keyType, TypeName valueType) {
    return ParameterizedTypeName.get(ClassName.get(Map.class), keyType, valueType);
  }
}
