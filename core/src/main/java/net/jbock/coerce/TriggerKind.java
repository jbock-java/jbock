package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;

public final class TriggerKind {

  public final CoercionKind kind;

  public final TypeMirror trigger;

  public final CoercionProvider.CollectorInfo collectorInfo;

  TriggerKind(CoercionKind kind, TypeMirror trigger, CoercionProvider.CollectorInfo collectorInfo) {
    this.kind = kind;
    this.trigger = trigger;
    this.collectorInfo = collectorInfo;
  }

  @Override
  public String toString() {
    return kind + ", " + trigger;
  }
}
