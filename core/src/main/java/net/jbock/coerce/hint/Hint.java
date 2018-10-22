package net.jbock.coerce.hint;

import javax.lang.model.type.TypeMirror;

public abstract class Hint {

  public abstract String message(TypeMirror type, boolean repeatable);
}
