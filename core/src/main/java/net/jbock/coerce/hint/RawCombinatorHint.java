package net.jbock.coerce.hint;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class RawCombinatorHint extends Hint {

  @Override
  public String message(TypeMirror mirror, boolean repeatable) {
    TypeTool tool = TypeTool.get();
    if (tool.isSameType(mirror, tool.erasure(List.class))) {
      return "Add a type parameter";
    }
    if (tool.isSameType(mirror, tool.erasure(Optional.class))) {
      return "Add a type parameter";
    }
    return null;
  }
}
