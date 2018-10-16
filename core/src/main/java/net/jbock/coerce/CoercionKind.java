package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public enum CoercionKind {

  SIMPLE(false),
  OPTIONAL_COMBINATION(true);

  private final boolean wrappedInOptional;

  CoercionKind(boolean wrappedInOptional) {
    this.wrappedInOptional = wrappedInOptional;
  }

  TriggerKind of(TypeMirror trigger, CollectorInfo collectorInfo) {
    return new TriggerKind(this, trigger, collectorInfo);
  }

  public boolean isWrappedInOptional() {
    return wrappedInOptional;
  }

  public static TriggerKind findKind(TypeMirror mirror) {
    TypeTool tool = TypeTool.get();
    if (tool.eql(tool.erasure(mirror), tool.declared(Optional.class))) {
      List<? extends TypeMirror> typeArgs = tool.typeargs(mirror);
      if (!typeArgs.isEmpty()) {
        return OPTIONAL_COMBINATION.of(typeArgs.get(0), CollectorInfo.empty());
      }
    }
    return CoercionKind.SIMPLE.of(mirror, CollectorInfo.empty());
  }
}
