package net.jbock.coerce.hint;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Set;

abstract class PreciseTypeHint extends Hint {

  @Override
  public final String message(TypeMirror mirror, boolean repeatable) {
    TypeName typeName = TypeName.get(mirror);
    if (types().contains(typeName)) {
      return message(typeName);
    }
    return null;
  }

  abstract String message(TypeName typeName);

  abstract Set<TypeName> types();

}
