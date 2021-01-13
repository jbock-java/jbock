package net.jbock.coerce;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

/**
 * Coercion input: Information about a single parameter (option or param).
 */
public class BasicInfo {

  private final EnumName paramName;

  private final ExecutableElement sourceMethod;

  private final TypeElement sourceElement;

  private final TypeTool tool;

  private final ClassName optionType;

  BasicInfo(
      EnumName paramName,
      ClassName optionType,
      ExecutableElement sourceMethod,
      TypeElement sourceElement,
      TypeTool tool) {
    this.paramName = paramName;
    this.optionType = optionType;
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.tool = tool;
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

  public EnumName parameterName() {
    return paramName;
  }

  public ParameterSpec constructorParam(TypeMirror type) {
    return ParameterSpec.builder(TypeName.get(type), paramName.camel()).build();

  }

  public TypeMirror returnType() {
    return sourceMethod.getReturnType();
  }

  public ValidationException failure(String message) {
    return ValidationException.create(sourceMethod, message);
  }

  public TypeTool tool() {
    return tool;
  }

  public ClassName optionType() {
    return optionType;
  }

  private boolean isEnumType(TypeMirror mirror) {
    Types types = tool.types();
    return types.directSupertypes(mirror).stream()
        .anyMatch(t -> tool.isSameErasure(t, Enum.class.getCanonicalName()));
  }

  public TypeElement sourceElement() {
    return sourceElement;
  }
}