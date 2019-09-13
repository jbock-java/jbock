package net.jbock.coerce.hint;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import java.util.List;

class RawCombinatorHint extends Hint {

  @Override
  String message(TypeElement type, boolean repeatable) {
    TypeTool tool = TypeTool.get();
    if (repeatable && tool.isSameType(type.asType(), tool.erasure(List.class))) {
      return "Add a type parameter";
    }
    return null;
  }
}
