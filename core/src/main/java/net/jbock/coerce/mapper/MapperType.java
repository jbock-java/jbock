package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public abstract class MapperType {

  private final boolean supplier; // wrapped in Supplier?
  private final List<TypeMirror> solution; // solved typevars of mapperClass

  MapperType(boolean supplier, List<TypeMirror> solution) {
    this.supplier = supplier;
    this.solution = solution;
  }

  public static ReferenceMapperType create(
      boolean supplier,
      TypeElement mapperClass,
      List<TypeMirror> solution,
      TypeMirror innerType) {
    return new ReferenceMapperType(mapperClass, supplier, solution, innerType);
  }

  public static AutoMapperType create(TypeMirror innerType, CodeBlock mapExpr) {
    return new AutoMapperType(innerType, mapExpr);
  }

  public abstract TypeMirror innerType();

  public abstract CodeBlock mapExpr();

  public boolean supplier() {
    return supplier;
  }

  public List<TypeMirror> solution() {
    return solution;
  }

}
