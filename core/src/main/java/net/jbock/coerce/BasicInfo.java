package net.jbock.coerce;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static javax.lang.model.element.Modifier.FINAL;

public class BasicInfo {

  private final ParamName paramName;

  private final ExecutableElement sourceMethod;

  private final TypeTool tool;

  private final Optional<TypeElement> mapperClass;

  private final Optional<TypeElement> collectorClass;

  private BasicInfo(
      ParamName paramName,
      ExecutableElement sourceMethod,
      TypeTool tool,
      Optional<TypeElement> mapperClass,
      Optional<TypeElement> collectorClass) {
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
    return new BasicInfo(paramName, sourceMethod, tool, Optional.ofNullable(mapperClass), Optional.ofNullable(collectorClass));
  }

  String paramName() {
    return paramName.camel();
  }

  ParamName parameterName() {
    return paramName;
  }

  // return type of the parameter method
  TypeMirror originalReturnType() {
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
    return mapperClass;
  }

  Optional<TypeElement> collectorClass() {
    return collectorClass;
  }
}
