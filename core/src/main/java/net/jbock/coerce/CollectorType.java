package net.jbock.coerce;

import net.jbock.compiler.ValidationException;

import javax.lang.model.type.TypeMirror;
import java.util.stream.Collector;

public final class CollectorType {

  private final TypeMirror type; // subtype of Collector
  private final boolean supplier; // wrapped in Supplier?

  private CollectorType(TypeMirror type, boolean supplier) {
    this.type = type;
    this.supplier = supplier;
  }

  static CollectorType create(BasicInfo basicInfo, TypeMirror type, boolean supplier) {
    if (!basicInfo.tool().isSameErasure(type, Collector.class)) {
      throw boom(basicInfo, "must either implement Collector or Supplier<Collector>");
    }
    if (basicInfo.tool().isRawType(type)) {
      throw boom(basicInfo, "the collector type must be parameterized");
    }
    return new CollectorType(type, supplier);
  }

  CollectorType solve(TypeMirror collectorClassSolved) {
    return new CollectorType(collectorClassSolved, supplier);
  }

  private static ValidationException boom(BasicInfo basicInfo, String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s", message));
  }

  public TypeMirror type() {
    return type;
  }

  public boolean supplier() {
    return supplier;
  }
}
