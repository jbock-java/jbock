package net.jbock.coerce.hint;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

abstract class Hint {

  abstract String message(TypeElement type, boolean repeatable);
}
