package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.mapper.AutoMapperType;
import net.jbock.coerce.mapper.MapperType;

import javax.lang.model.type.TypeMirror;

public abstract class CoercionFactory {

  /**
   * Creates a function that maps from String to innerType
   */
  public abstract CodeBlock createMapper(TypeMirror innerType);

  public CodeBlock initMapper(MapperType mapperType, TypeMirror innerType) {
    if (mapperType instanceof AutoMapperType) {
      return ((AutoMapperType) mapperType).createExpression();
    }
    // TODO reference mapper
    return createMapper(innerType);
  }
}
