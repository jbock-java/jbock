package net.jbock.coerce.mappers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CollectorInfo;
import net.jbock.coerce.OptionalInfo;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.compiler.Constants.STRING;

public final class MapperCoercion extends CoercionFactory {

  private final ParameterSpec mapperParam;

  private final TypeMirror mapperType;

  private MapperCoercion(TypeMirror mapperReturnType, ParameterSpec mapperParam, TypeMirror mapperType) {
    super(mapperReturnType);
    this.mapperParam = mapperParam;
    this.mapperType = mapperType;
  }

  public static Coercion create(
      OptionalInfo optionalInfo,
      CollectorInfo collectorInfo,
      ParameterSpec mapperParam,
      TypeMirror mapperType,
      BasicInfo basicInfo) {
    return create(optionalInfo, Optional.of(collectorInfo), mapperParam, mapperType, basicInfo);
  }

  public static Coercion create(
      OptionalInfo optionalInfo,
      ParameterSpec mapperParam,
      TypeMirror mapperType,
      BasicInfo basicInfo) {
    return create(optionalInfo, Optional.empty(), mapperParam, mapperType, basicInfo);
  }

  private static Coercion create(
      OptionalInfo optionalInfo,
      Optional<CollectorInfo> collectorInfo,
      ParameterSpec mapperParam,
      TypeMirror mapperType,
      BasicInfo basicInfo) {
    return new MapperCoercion(optionalInfo.baseType, mapperParam, mapperType)
        .getCoercion(basicInfo, optionalInfo, collectorInfo);
  }

  @Override
  public CodeBlock map() {
    return mapperMap(mapperParam);
  }

  @Override
  public CodeBlock initMapper() {
    return mapperInit(mapperReturnType, mapperParam, mapperType);
  }

  private static CodeBlock mapperMap(ParameterSpec mapperParam) {
    return CodeBlock.builder().add(".map($N)", mapperParam).build();
  }

  private static CodeBlock mapperInit(
      TypeMirror mapperReturnType,
      ParameterSpec mapperParam,
      TypeMirror mapperType) {
    return mapperInit(TypeName.get(mapperReturnType), mapperParam, mapperType);
  }

  private static CodeBlock mapperInit(
      TypeName mapperReturnType,
      ParameterSpec mapperParam,
      TypeMirror mapperType) {
    return CodeBlock.builder()
        .add("$T $N = new $T().get()",
            ParameterizedTypeName.get(ClassName.get(Function.class), STRING, mapperReturnType),
            mapperParam,
            mapperType)
        .build();
  }
}
