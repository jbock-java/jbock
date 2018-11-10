package net.jbock.coerce.hint;

import javax.lang.model.type.TypeMirror;

abstract class Hint {

  abstract String message(TypeMirror type, boolean repeatable);
}
