package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;

final class TriggerKind {

  final CoercionKind kind;
  final TypeMirror trigger;

  TriggerKind(CoercionKind kind, TypeMirror trigger) {
    this.kind = kind;
    this.trigger = trigger;
  }

  @Override
  public String toString() {
    return kind + ", " + trigger;
  }
}
