package net.jbock.coerce.coercions;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.mapper.AutoMapperType;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.compiler.Constants.STRING;

public abstract class CoercionFactory {

  /**
   * Creates a function that maps from String to innerType
   */
  public abstract CodeBlock createMapper(TypeMirror innerType);

  private CodeBlock initMapper(MapperType mapperType, TypeMirror innerType, String paramName) {
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

  private String mapperParamName(String paramName) {
    return paramName + "Mapper";
  }

  public final Coercion getCoercion(
      BasicInfo basicInfo,
      Optional<AbstractCollector> collector,
      MapperType mapperType,
      Function<ParameterSpec, CodeBlock> extractExpr,
      TypeMirror constructorParamType) {
    TypeMirror innerType = innerType(mapperType);
    CodeBlock mapExpr = CodeBlock.of("$L", mapperParamName(basicInfo.paramName()));
    CodeBlock initMapper = initMapper(mapperType, innerType, basicInfo.paramName());
    return Coercion.create(
        mapExpr,
        initMapper,
        collector,
        constructorParamType,
        basicInfo,
        mapperType.isOptional(),
        extractExpr);
  }

  private TypeMirror innerType(MapperType mapperType) {
    if (mapperType instanceof AutoMapperType) {
      return ((AutoMapperType) mapperType).innerType();
    }
    if (mapperType instanceof ReferenceMapperType) {
      return ((ReferenceMapperType) mapperType).innerType();
    }
    throw new AssertionError("all cases handled");
  }
}
