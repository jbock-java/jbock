package net.jbock.coerce.hint;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;

class RawCombinatorHint extends Hint {

  @Override
  String message(TypeMirror mirror, boolean repeatable) {
    TypeTool tool = TypeTool.get();
    if (repeatable && tool.isSameType(mirror, tool.erasure(List.class))) {
      return "Add a type parameter";
    }
    return null;
  }
}
