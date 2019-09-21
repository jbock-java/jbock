package net.jbock.coerce.mapper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class ReferenceMapperType extends MapperType {

  private final TypeElement mapperClass; // implements Function or Supplier<Function>
  private final TypeMirror innerType; // what the function returns

  ReferenceMapperType(
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution,
      boolean optional,
      TypeMirror innerType) {
    super(supplier, solution, optional);
    this.mapperClass = mapperClass;
    this.innerType = innerType;
  }

  @Override
  public TypeMirror mapperType() {
    return mapperClass.asType();
  }

  public TypeMirror innerType() {
    return innerType;
  }
}
