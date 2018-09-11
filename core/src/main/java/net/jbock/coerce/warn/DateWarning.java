package net.jbock.coerce.warn;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Calendar;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateWarning extends PreciseTypeWarning {

  @Override
  public Set<TypeName> types() {
    return Stream.of(
        java.util.Date.class,
        java.sql.Date.class,
        Calendar.class
    ).map(ClassName::get).collect(Collectors.toSet());
  }

  @Override
  public String message(TypeName typeName) {
    return typeName + " is not supported. Use a type from java.time instead.";
  }
}
