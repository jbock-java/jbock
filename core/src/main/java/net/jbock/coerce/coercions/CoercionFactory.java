package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.mapper.AutoMapperType;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;

import javax.lang.model.type.TypeMirror;

public abstract class CoercionFactory {

  /**
   * Creates a function that maps from String to innerType
   */
  public abstract CodeBlock createMapper(TypeMirror innerType);

  public CodeBlock initMapper(MapperType mapperType, TypeMirror innerType, String paramName) {
    if (mapperType instanceof AutoMapperType) {
      return ((AutoMapperType) mapperType).createExpression();
    }
    // TODO reference mapper
    return createMapper(innerType);
  }

  public String mapperParamName(String paramName) {
    return paramName + "Mapper";
  }

  public TypeMirror innerType(MapperType mapperType) {
    if (mapperType instanceof AutoMapperType) {
      return ((AutoMapperType) mapperType).innerType();
    }
    if (mapperType instanceof ReferenceMapperType) {
      return ((ReferenceMapperType) mapperType).innerType();
    }
    throw new AssertionError("all cases handled");
  }
}
