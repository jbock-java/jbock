package net.jbock.coerce.hint;

import javax.lang.model.element.TypeElement;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DateHint extends PreciseTypeHint {

  private static final Set<Class<?>> TYPES = Stream.of(
      Date.class,
      java.sql.Date.class,
      Calendar.class
  ).collect(Collectors.toSet());

  @Override
  Set<Class<?>> types() {
    return TYPES;
  }

  @Override
  String message(TypeElement type) {
    return type + " is not supported. Use a type from java.time instead.";
  }
}
