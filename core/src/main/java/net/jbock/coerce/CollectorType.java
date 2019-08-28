package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.stream.Collector;

public final class CollectorType {

  private final TypeTool tool;

  private final TypeElement collectorClass; // implements Collector or Supplier<Collector>
  private final TypeMirror type; // subtype of Collector
  private final boolean supplier; // wrapped in Supplier?

  private CollectorType(TypeTool tool, TypeElement collectorClass, TypeMirror type, boolean supplier) {
    this.tool = tool;
    this.collectorClass = collectorClass;
    this.type = type;
    this.supplier = supplier;
  }

  static CollectorType create(BasicInfo basicInfo, TypeMirror type, boolean supplier, TypeElement collectorClass) {
    if (!basicInfo.tool().isSameErasure(type, Collector.class)) {
      throw boom(basicInfo, "must either implement Collector or Supplier<Collector>");
    }
    if (basicInfo.tool().isRawType(type)) {
      throw boom(basicInfo, "the collector type must be parameterized");
    }
    return new CollectorType(basicInfo.tool(), collectorClass, type, supplier);
  }

  private static ValidationException boom(BasicInfo basicInfo, String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s", message));
  }

  TypeMirror type() {
    return type;
  }

  public TypeMirror collectorType() {
    return tool.erasure(collectorClass.asType());
  }

  public boolean supplier() {
    return supplier;
  }
}
