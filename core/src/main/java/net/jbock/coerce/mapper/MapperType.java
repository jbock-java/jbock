package net.jbock.coerce.mapper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * Info about the mapper class.
 */
public abstract class MapperType {

  private final TypeElement mapperClass; // implements Function or Supplier<Function>
  private final boolean supplier; // wrapped in Supplier?
  private final List<TypeMirror> solution; // solved typevars of mapperClass

  MapperType(TypeElement mapperClass, boolean supplier, List<TypeMirror> solution) {
    this.mapperClass = mapperClass;
    this.supplier = supplier;
    this.solution = solution;
  }

  public TypeMirror mapperType() {
    return mapperClass.asType();
  }

  public boolean supplier() {
    return supplier;
  }

  public List<TypeMirror> solution() {
    return solution;
  }
}
