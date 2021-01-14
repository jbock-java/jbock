package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.matching.AutoMatcher;
import net.jbock.coerce.matching.MapperMatcher;
import net.jbock.coerce.matching.Matcher;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.ValidationException;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

/**
 * Coercion input: Information about a single parameter (option or param).
 */
public class BasicInfo extends ParameterScoped {

  @Inject
  BasicInfo(ParameterContext context) {
    super(context);
  }

  public Coercion nonFlagCoercion() {
    return mapperClass()
        .<Matcher>map(mapper -> new MapperMatcher(this, mapper))
        .orElseGet(() -> new AutoMatcher(this))
        .findCoercion();
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

  @Deprecated
  public EnumName parameterName() {
    return enumName();
  }

  public ParameterSpec constructorParam(TypeMirror type) {
    return ParameterSpec.builder(TypeName.get(type), parameterName().camel()).build();

  }

  public TypeMirror returnType() {
    return sourceMethod().getReturnType();
  }

  public ValidationException failure(String message) {
    return ValidationException.create(sourceMethod(), message);
  }

  private boolean isEnumType(TypeMirror mirror) {
    Types types = tool().types();
    return types.directSupertypes(mirror).stream()
        .anyMatch(t -> tool().isSameErasure(t, Enum.class.getCanonicalName()));
  }
}