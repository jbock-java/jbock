package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public final class CollectorType {

  private final TypeTool tool;

  private final TypeElement collectorClass; // implements Collector or Supplier<Collector>
  private final boolean supplier; // wrapped in Supplier?

  private final List<TypeMirror> solution; // solved typevars of collectorClass

  private CollectorType(TypeTool tool, TypeElement collectorClass, boolean supplier, List<TypeMirror> solution) {
    this.tool = tool;
    this.collectorClass = collectorClass;
    this.supplier = supplier;
    this.solution = solution;
  }

  static CollectorType create(
      BasicInfo basicInfo,
      boolean supplier,
      TypeElement collectorClass,
      List<TypeMirror> solution) {
    return new CollectorType(basicInfo.tool(), collectorClass, supplier, solution);
  }

  public TypeMirror collectorType() {
    return tool.erasure(collectorClass.asType());
  }

  public boolean supplier() {
    return supplier;
  }

  public List<TypeMirror> solution() {
    return solution;
  }
}
