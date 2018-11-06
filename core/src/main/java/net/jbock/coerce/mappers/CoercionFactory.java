package net.jbock.coerce.mappers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CollectorInfo;
import net.jbock.coerce.OptionalInfo;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

public abstract class CoercionFactory {

  public boolean handlesOptionalPrimitive() {
    return false;
  }

  final TypeMirror mapperReturnType;

  CoercionFactory(Class<?> mapperReturnType) {
    this(TypeTool.get().getTypeElement(mapperReturnType).asType());
  }

  CoercionFactory(TypeMirror mapperReturnType) {
    this.mapperReturnType = mapperReturnType;
  }

  /**
   * Maps from String to mapperReturnType
   */
  abstract CodeBlock map();

  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$T.requireNonNull($N)", Objects.class, param).build();
  }

  final TypeMirror mapperReturnType() {
    return mapperReturnType;
  }

  CodeBlock initMapper() {
    return CodeBlock.builder().build();
  }

  TypeMirror paramType() {
    return mapperReturnType;
  }

  public final Coercion getCoercion(
      BasicInfo basicInfo,
      OptionalInfo optionalInfo,
      Optional<CollectorInfo> collectorInfo) {
    return getCoercion(basicInfo, optionalInfo, collectorInfo, map(), initMapper());
  }

  private Coercion getCoercion(
      BasicInfo basicInfo,
      OptionalInfo optionalInfo,
      Optional<CollectorInfo> collectorInfo,
      CodeBlock map,
      CodeBlock initMapper) {
    CodeBlock extract;
    TypeMirror paramType;
    if (optionalInfo.optional || collectorInfo.isPresent()) {
      paramType = basicInfo.returnType;
      ParameterSpec param = ParameterSpec.builder(TypeName.get(paramType), basicInfo.paramName()).build();
      extract = CodeBlock.builder().add("$T.requireNonNull($N)", Objects.class, param).build();
    } else {
      paramType = paramType();
      ParameterSpec param = ParameterSpec.builder(TypeName.get(paramType), basicInfo.paramName()).build();
      extract = extract(param);
    }
    return Coercion.create(
        collectorParam(basicInfo, collectorInfo),
        map,
        initMapper,
        initCollector(collectorInfo),
        extract,
        paramType,
        basicInfo);
  }

  private CodeBlock initCollector(
      Optional<CollectorInfo> collectorInfo) {
    if (!collectorInfo.isPresent()) {
      return CodeBlock.builder().build();
    }
    return collectorInfo.get().collectorInit();
  }

  private Optional<ParameterSpec> collectorParam(
      BasicInfo basicInfo,
      Optional<CollectorInfo> collectorInfo) {
    if (!collectorInfo.isPresent()) {
      return Optional.empty();
    }
    TypeName t = TypeName.get(collectorInfo.get().inputType);
    TypeName a = WildcardTypeName.subtypeOf(Object.class);
    TypeName r = TypeName.get(basicInfo.returnType);
    return Optional.of(ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Collector.class), t, a, r), basicInfo.paramName() + "Collector")
        .build());
  }
}
