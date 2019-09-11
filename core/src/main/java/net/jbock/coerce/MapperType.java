package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public final class MapperType {


  private final TypeElement mapperClass; // implements Function or Supplier<Function>
  private final boolean supplier; // wrapped in Supplier?

  private final List<TypeMirror> solution; // solved typevars of mapperClass

  private MapperType(
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution) {
    this.mapperClass = mapperClass;
    this.supplier = supplier;
    this.solution = solution;
  }

  static MapperType create(
      boolean supplier,
      TypeElement mapperClass,
      List<TypeMirror> solution) {
    return new MapperType(mapperClass, supplier, solution);
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
