package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

public final class MapperType {

  private final TypeTool tool;

  private final TypeElement mapperClass; // implements Function or Supplier<Function>
  private final TypeMirror type; // subtype of Function
  private final boolean supplier; // wrapped in Supplier?

  private MapperType(TypeTool tool, TypeElement mapperClass, TypeMirror type, boolean supplier) {
    this.tool = tool;
    this.mapperClass = mapperClass;
    this.type = type;
    this.supplier = supplier;
  }

  static MapperType create(
      BasicInfo basicInfo,
      TypeMirror type,
      boolean supplier,
      TypeElement mapperClass) {
    if (!basicInfo.tool().isSameErasure(type, Function.class)) {
      throw boom(basicInfo, "must either implement Function or Supplier<Function>");
    }
    if (basicInfo.tool().isRawType(type)) {
      throw boom(basicInfo, "the function type must be parameterized");
    }
    return new MapperType(basicInfo.tool(), mapperClass, type, supplier);
  }

  private static ValidationException boom(BasicInfo basicInfo, String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s", message));
  }

  public TypeMirror mapperType() {
    return tool.erasure(mapperClass.asType());
  }

  TypeMirror type() {
    return type;
  }

  public boolean supplier() {
    return supplier;
  }
}
