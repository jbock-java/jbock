package net.jbock.coerce.warn;

import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Set;

abstract class PreciseTypeWarning extends Warning {

  @Override
  public final String message(TypeMirror mirror) {
    TypeName typeName = TypeName.get(mirror);
    if (types().contains(typeName)) {
      return message(typeName);
    }
    return null;
  }

  abstract String message(TypeName typeName);

  abstract Set<TypeName> types();

}
