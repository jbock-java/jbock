package net.jbock.coerce.reference;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * A declared type, with extra type information about its erasure.
 *
 * @param <E> the erasure type
 */
public class Declared<E> {

  private final Class<E> erasure;

  private final List<? extends TypeMirror> typeArguments;

  Declared(Class<E> erasure, List<? extends TypeMirror> typeArguments) {
    this.erasure = erasure;
    this.typeArguments = typeArguments;
  }

  public DeclaredType asType(TypeTool tool) {
    return tool.getDeclaredType(erasure, typeArguments);
  }

  List<? extends TypeMirror> typeArguments() {
    return typeArguments;
  }
}
