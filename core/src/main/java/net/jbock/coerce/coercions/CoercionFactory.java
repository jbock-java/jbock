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

  private CodeBlock mapExpr(String paramName) {
    return CodeBlock.of("$L", mapperParamName(paramName));
  }

  /**
   * Creates a function that maps from String to innerType
   */
  public abstract CodeBlock createMapper(TypeMirror innerType);

  private CodeBlock initMapper(TypeMirror innerType, String paramName) {
    ParameterSpec mapperParam = mapperParam(innerType, paramName);
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
      Optional<MapperType> mapperType) {
    TypeMirror innerType = innerType(basicInfo, mapperType, collector);
    CodeBlock mapExpr = mapExpr(basicInfo.paramName());
    CodeBlock initMapper = initMapper(innerType, basicInfo.paramName());
    TypeMirror constructorParamType = basicInfo.returnType();
    return Coercion.create(
        mapExpr,
        initMapper,
        collector,
        constructorParamType,
        basicInfo,
        mapperType.map(MapperType::isOptional).orElseGet(() -> basicInfo.optionalInfo().isPresent()));
  }

  private TypeMirror innerType(BasicInfo basicInfo, Optional<MapperType> mapperType, Optional<AbstractCollector> collector) {
    if (mapperType.isPresent()) {
      MapperType type = mapperType.get();
      if (type instanceof AutoMapperType) {
        return ((AutoMapperType) type).innerType();
      }
      if (type instanceof ReferenceMapperType) {
        return ((ReferenceMapperType) type).innerType();
      }
      throw new AssertionError("all cases handled");
    }
    return collector.map(AbstractCollector::inputType).orElse(basicInfo.optionalInfo().orElse(basicInfo.returnType()));
  }
}
