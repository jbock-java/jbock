package net.jbock.coerce.mapper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class EnhancedMapperType extends MapperType {

  private EnhancedMapperType(
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution) {
    super(mapperClass, supplier, solution);
  }

  public static EnhancedMapperType create(
      boolean supplier,
      TypeElement mapperClass,
      List<TypeMirror> solution) {
    return new EnhancedMapperType(mapperClass, supplier, solution);
  }
}
