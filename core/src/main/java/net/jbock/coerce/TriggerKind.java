package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;

import static java.util.Objects.requireNonNull;

public final class TriggerKind {

  public final CoercionKind kind;

  public final TypeMirror trigger;

  public final CollectorInfo collectorInfo;

  TriggerKind(CoercionKind kind, TypeMirror trigger, CollectorInfo collectorInfo) {
    this.kind = requireNonNull(kind);
    this.trigger = requireNonNull(trigger);
    this.collectorInfo = requireNonNull(collectorInfo);
  }

  @Override
  public String toString() {
    return kind + ", " + trigger;
  }
}
