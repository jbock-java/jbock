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

  private final List<ImplementsRelation> path;

  Declared(Class<E> erasure, List<? extends TypeMirror> typeArguments, List<ImplementsRelation> path) {
    this.erasure = erasure;
    this.typeArguments = typeArguments;
    this.path = path;
  }

  public DeclaredType asType(TypeTool tool) {
    return tool.getDeclaredType(erasure, typeArguments);
  }

  public List<? extends TypeMirror> typeArguments() {
    return typeArguments;
  }

  boolean isDirect() {
    return path.isEmpty();
  }
}
