package net.jbock.coerce.hint;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Calendar;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DateHint extends PreciseTypeHint {

  @Override
  Set<TypeName> types() {
    return Stream.of(
        java.util.Date.class,
        java.sql.Date.class,
        Calendar.class
    ).map(ClassName::get).collect(Collectors.toSet());
  }

  @Override
  String message(TypeName typeName) {
    return typeName + " is not supported. Use a type from java.time instead.";
  }
}
