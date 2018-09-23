package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;

public final class TriggerKind {

  public final CoercionKind kind;
  public final TypeMirror trigger;

  TriggerKind(CoercionKind kind, TypeMirror trigger) {
    this.kind = kind;
    this.trigger = trigger;
  }

  @Override
  public String toString() {
    return kind + ", " + trigger;
  }
}
