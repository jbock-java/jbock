package net.jbock.coerce.hint;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;

class CollectionHint extends Hint {

  @Override
  String message(TypeMirror type, boolean repeatable) {
    if (TypeTool.get().isSameErasure(type, List.class)) {
      if (!repeatable) {
        return "Declare this parameter repeatable.";
      } else {
        return "Define a custom mapper.";
      }
    }
    return null;
  }
}
