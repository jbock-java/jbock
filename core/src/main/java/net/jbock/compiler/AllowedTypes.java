package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

final class AllowedTypes {

  private static final List<TypeName> BASICS = Arrays.asList(Constants.INTEGER, Constants.STRING);

  private static final List<ClassName> COMBINATORS = Arrays.asList(ClassName.get(Optional.class), ClassName.get(List.class));

  private static final List<TypeName> IRREGULAR = Arrays.asList(TypeName.get(OptionalInt.class), TypeName.BOOLEAN, TypeName.INT);

  static List<TypeName> getAllowed() {
    List<TypeName> result = new ArrayList<>(IRREGULAR.size() + BASICS.size() * COMBINATORS.size());
    result.addAll(IRREGULAR);
    for (ClassName combinator : COMBINATORS) {
      for (TypeName typeName : BASICS) {
        result.add(ParameterizedTypeName.get(combinator, typeName));
      }
    }
    return result;
  }
}
