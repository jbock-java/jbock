package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.Util.QUALIFIED_NAME;

public enum CoercionKind {

  SIMPLE(false),
  OPTIONAL_COMBINATION(true);

  public final boolean combination;

  CoercionKind(boolean combination) {
    this.combination = combination;
  }

  TriggerKind of(TypeMirror mirror, CoercionProvider.CollectorInfo collectorInfo) {
    return new TriggerKind(this, mirror, collectorInfo);
  }

  public boolean isCombination() {
    return combination;
  }

  public static CoercionKind findKind(TypeMirror mirror) {
    String qname = mirror.accept(QUALIFIED_NAME, null);
    switch (qname) {
      case "java.util.Optional":
        return CoercionKind.OPTIONAL_COMBINATION;
      default:
        return CoercionKind.SIMPLE;
    }
  }
}
