package net.jbock.coerce.warn;

import javax.lang.model.type.TypeMirror;

public abstract class Warning {

  public abstract String message(TypeMirror type, boolean repeatable);
}
