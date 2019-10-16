package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

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
      TypeTool tool,
      boolean supplier,
      TypeElement mapperClass,
      List<TypeMirror> solution) {
    return new ReferenceMapperType(tool, mapperClass, supplier, solution);
  }

  public static AutoMapperType create(CodeBlock mapExpr) {
    return new AutoMapperType(mapExpr);
  }

  public abstract CodeBlock mapExpr();

  public boolean supplier() {
    return supplier;
  }

  public List<TypeMirror> solution() {
    return solution;
  }

}
