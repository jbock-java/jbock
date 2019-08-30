package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public final class MapperType {

  private final TypeTool tool;

  private final TypeElement mapperClass; // implements Function or Supplier<Function>
  private final boolean supplier; // wrapped in Supplier?

  private final List<TypeMirror> solution; // solved typevars of mapperClass

  private MapperType(
      TypeTool tool,
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution) {
    this.tool = tool;
    this.mapperClass = mapperClass;
    this.supplier = supplier;
    this.solution = solution;
  }

  static MapperType create(
      BasicInfo basicInfo,
      boolean supplier,
      TypeElement mapperClass,
      List<TypeMirror> solution) {
    return new MapperType(basicInfo.tool(), mapperClass, supplier, solution);
  }

  public TypeMirror mapperType() {
    return tool.erasure(mapperClass.asType());
  }

  public boolean hasTypeParams() {
    return !mapperClass.getTypeParameters().isEmpty();
  }

  public boolean supplier() {
    return supplier;
  }

  public List<TypeMirror> solution() {
    return solution;
  }
}
