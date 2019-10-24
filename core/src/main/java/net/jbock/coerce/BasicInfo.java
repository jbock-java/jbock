package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.FINAL;

public class BasicInfo {

  private final ParamName paramName;

  private final ExecutableElement sourceMethod;

  private final TypeTool tool;

  // nullable
  private final TypeElement mapperClass;

  // nullable
  private final TypeElement collectorClass;

  private BasicInfo(
      ParamName paramName,
      ExecutableElement sourceMethod,
      TypeTool tool,
      TypeElement mapperClass,
      TypeElement collectorClass) {
    this.paramName = paramName;
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.mapperClass = mapperClass;
    this.collectorClass = collectorClass;
  }

  static BasicInfo create(
      TypeElement mapperClass,
      TypeElement collectorClass,
      ParamName paramName,
      ExecutableElement sourceMethod,
      TypeTool tool) {
    return new BasicInfo(paramName, sourceMethod, tool, mapperClass, collectorClass);
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

  public Optional<CodeBlock> findMapExpr(TypeMirror innerType) {
    Optional<CodeBlock> mapExpr = AutoMapper.findAutoMapper(tool(), innerType);
    if (mapExpr.isPresent()) {
      return mapExpr;
    }
    if (isEnumType(innerType)) {
      return Optional.of(CodeBlock.of("$T::valueOf", innerType));
    }
    return Optional.empty();
  }

  String paramName() {
    return paramName.camel();
  }

  ParamName parameterName() {
    return paramName;
  }

  // return type of the parameter method
  public TypeMirror originalReturnType() {
    return sourceMethod.getReturnType();
  }

  FieldSpec fieldSpec() {
    return FieldSpec.builder(TypeName.get(originalReturnType()), paramName.camel(), FINAL).build();
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
}
