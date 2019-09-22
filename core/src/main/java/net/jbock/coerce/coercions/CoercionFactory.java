package net.jbock.coerce.coercions;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.mapper.AutoMapperType;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

import static net.jbock.compiler.Constants.STRING;

public abstract class CoercionFactory {

  /**
   * Creates a function that maps from String to innerType
   */
  public abstract CodeBlock createMapper(TypeMirror innerType);

  public CodeBlock initMapper(MapperType mapperType, TypeMirror innerType, String paramName) {
    ParameterSpec mapperParam = mapperParam(innerType, paramName);
    if (mapperType instanceof AutoMapperType) {
      return CodeBlock.of("$T $N = $L",
          mapperParam.type,
          mapperParam,
          ((AutoMapperType) mapperType).createExpression());
    }
    // TODO reference mapper
    return CodeBlock.of("$T $N = $L",
        mapperParam.type,
        mapperParam,
        createMapper(innerType));
  }

  private ParameterSpec mapperParam(TypeMirror innerType, String paramName) {
    ParameterizedTypeName mapperParamType = ParameterizedTypeName.get(
        ClassName.get(Function.class), STRING,
        TypeName.get(innerType));
    return ParameterSpec.builder(mapperParamType, mapperParamName(paramName)).build();
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
