package net.jbock.coerce.mapper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class EnhancedMapperType extends MapperType {

  private final boolean optional;

  private EnhancedMapperType(
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution,
      boolean optional) {
    super(mapperClass, supplier, solution);
    this.optional = optional;
  }

  public static EnhancedMapperType create(
      boolean supplier,
      boolean optional,
      TypeElement mapperClass,
      List<TypeMirror> solution) {
    return new EnhancedMapperType(mapperClass, supplier, solution, optional);
  }

  public boolean isOptional() {
    return optional;
  }
}
