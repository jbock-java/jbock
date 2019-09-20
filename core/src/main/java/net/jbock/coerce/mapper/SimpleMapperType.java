package net.jbock.coerce.mapper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class SimpleMapperType extends MapperType {

  private SimpleMapperType(
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution) {
    super(mapperClass, supplier, solution);
  }

  public static SimpleMapperType create(
      boolean supplier,
      TypeElement mapperClass,
      List<TypeMirror> solution) {
    return new SimpleMapperType(mapperClass, supplier, solution);
  }
}
