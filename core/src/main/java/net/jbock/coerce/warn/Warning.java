package net.jbock.coerce.warn;

import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public abstract class Warning {

  public abstract String message(TypeMirror type);
}
