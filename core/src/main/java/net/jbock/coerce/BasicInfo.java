package net.jbock.coerce;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.collectorpresent.CollectorClassValidator;
import net.jbock.coerce.collectors.AbstractCollector;
import net.jbock.coerce.collectors.DefaultCollector;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class BasicInfo {

  private final ParamName paramName;

  private final ExecutableElement sourceMethod;

  private final TypeTool tool;

  private final ClassName optionType;

  // nullable
  private final TypeElement mapperClass;

  // nullable
  private final TypeElement collectorClass;

  private BasicInfo(
      ParamName paramName,
      ExecutableElement sourceMethod,
      TypeTool tool,
      ClassName optionType,
      TypeElement mapperClass,
      TypeElement collectorClass) {
    this.paramName = paramName;
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.optionType = optionType;
    this.mapperClass = mapperClass;
    this.collectorClass = collectorClass;
  }

  static BasicInfo create(
      Optional<TypeElement> mapperClass,
      Optional<TypeElement> collectorClass,
      ParamName paramName,
      ClassName optionType,
      ExecutableElement sourceMethod,
      TypeTool tool) {
    return new BasicInfo(paramName, sourceMethod, tool, optionType, mapperClass.orElse(null), collectorClass.orElse(null));
  }

  private boolean isEnumType(TypeMirror mirror) {
    List<? extends TypeMirror> supertypes = tool().getDirectSupertypes(mirror);
    if (supertypes.isEmpty()) {
      // not an enum
      return false;
    }
    TypeMirror superclass = supertypes.get(0);
    if (!tool().isSameErasure(superclass, Enum.class)) {
      // not an enum
      return false;
    }
    return !tool().isPrivateType(mirror);
  }

  public Optional<CodeBlock> findAutoMapper(TypeMirror testType) {
    Optional<CodeBlock> mapExpr = AutoMapper.findAutoMapper(tool(), testType);
    if (mapExpr.isPresent()) {
      return mapExpr;
    }
    if (isEnumType(testType)) {
      return Optional.of(CodeBlock.of("$T::valueOf", testType));
    }
    return Optional.empty();
  }

  public ParamName parameterName() {
    return paramName;
  }

  public ParameterSpec constructorParam(TypeMirror type) {
    return ParameterSpec.builder(TypeName.get(type), paramName.camel()).build();

  }

  // return type of the parameter method
  public TypeMirror originalReturnType() {
    return sourceMethod.getReturnType();
  }

  FieldSpec fieldSpec() {
    return FieldSpec.builder(TypeName.get(originalReturnType()), paramName.camel()).build();
  }

  public ValidationException asValidationException(String message) {
    return ValidationException.create(sourceMethod, message);
  }

  public TypeTool tool() {
    return tool;
  }

  Optional<TypeElement> mapperClass() {
    return Optional.ofNullable(mapperClass);
  }

  Optional<TypeElement> collectorClass() {
    return Optional.ofNullable(collectorClass);
  }

  public AbstractCollector collectorInfo() {
    if (collectorClass().isPresent()) {
      return new CollectorClassValidator(this, collectorClass().get()).getCollectorInfo();
    }
    Optional<TypeMirror> wrapped = tool().unwrap(List.class, originalReturnType());
    if (!wrapped.isPresent()) {
      throw asValidationException("Either define a custom collector, or return List.");
    }
    return new DefaultCollector(wrapped.get());
  }

  public ClassName optionType() {
    return optionType;
  }
}
