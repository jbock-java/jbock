package net.jbock.coerce.mappers;

import java.util.regex.Pattern;

class PatternCoercion extends SimpleCoercion {

  PatternCoercion() {
    super(Pattern.class, "compile");
  }

}
