package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;

import java.util.List;
import java.util.Optional;

public class TypeInfo {

  private final Coercion coercion;
  private final boolean repeatable;
  private final boolean required;
  private final boolean flag;

  private final TypeName typeName;

  private TypeInfo(
      TypeName typeName,
      Coercion coercion,
      boolean repeatable,
      boolean required,
      boolean flag) {
    this.coercion = coercion;
    this.repeatable = repeatable;
    this.required = required;
    this.flag = flag;
    this.typeName = typeName;
  }

  private static TypeInfo createRepeatable(Coercion coercion, TypeName typeName) {
    return new TypeInfo(typeName, coercion, true, false, false);
  }

  private static TypeInfo createRequired(Coercion coercion, TypeName typeName) {
    return new TypeInfo(typeName, coercion, false, true, false);
  }

  private static TypeInfo createOptional(Coercion coercion, TypeName typeName) {
    return new TypeInfo(typeName, coercion, false, false, false);
  }

  private static TypeInfo createFlag(Coercion coercion, TypeName typeName) {
    return new TypeInfo(typeName, coercion, false, false, true);
  }

  static TypeInfo create(TypeName typeName, Coercion coercion) {
    if (Constants.STRING_ARRAY.equals(typeName)) {
      return createRepeatable(coercion, typeName);
    }
    if (flag(typeName)) {
      return createFlag(coercion, typeName);
    }
    if (typeName.isPrimitive()) {
      return createRequired(coercion, typeName);
    }
    // special = primitive or optional
    if (coercion.special()) {
      return createOptional(coercion, typeName);
    }
    if (typeName instanceof ParameterizedTypeName) {
      return findParameterizedTypeInfo(coercion, (ParameterizedTypeName) typeName);
    }
    // otherwise it must be required
    return createRequired(coercion, typeName);
  }

  private static boolean flag(TypeName typeName) {
    return TypeName.BOOLEAN.equals(typeName) ||
        ClassName.get(Boolean.class).equals(typeName);
  }

  public boolean flag() {
    return flag;
  }

  public Coercion coercion() {
    return coercion;
  }

  public boolean array() {
    return Constants.STRING_ARRAY.equals(typeName);
  }

  public boolean repeatable() {
    return repeatable;
  }

  public boolean required() {
    return required;
  }

  private static TypeInfo findParameterizedTypeInfo(
      Coercion coercion,
      ParameterizedTypeName parameterizedType) {
    ClassName rawType = parameterizedType.rawType;
    if (ClassName.get(Optional.class).equals(rawType)) {
      return createOptional(coercion, parameterizedType);
    }
    if (ClassName.get(List.class).equals(rawType)) {
      return createRepeatable(coercion, parameterizedType);
    }
    throw new AssertionError("we should never get here");
  }
}
