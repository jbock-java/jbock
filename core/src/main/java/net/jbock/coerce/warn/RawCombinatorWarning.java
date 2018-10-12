package net.jbock.coerce.warn;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class RawCombinatorWarning extends Warning {

  @Override
  public String message(TypeMirror mirror, boolean repeatable) {
    TypeTool tool = TypeTool.get();
    if (tool.eql(mirror, tool.declared(List.class))) {
      return "Add a type parameter";
    }
    if (tool.eql(mirror, tool.declared(Optional.class))) {
      return "Add a type parameter";
    }
    return null;
  }
}
