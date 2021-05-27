package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.common.EnumName;
import net.jbock.parameter.AbstractParameter;

import java.util.Locale;
import java.util.Optional;

public final class ConvertedParameter<P extends AbstractParameter> {

  private final Optional<CodeBlock> mapExpr;
  private final Optional<CodeBlock> extractExpr;
  private final Skew skew;
  private final P parameter;
  private final ParameterSpec asParameterSpec;
  private final FieldSpec asFieldSpec;

  private ConvertedParameter(
      Optional<CodeBlock> mapExpr,
      Optional<CodeBlock> extractExpr,
      Skew skew,
      ParameterSpec asParameterSpec,
      FieldSpec asFieldSpec,
      P parameter) {
    this.asParameterSpec = asParameterSpec;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.asFieldSpec = asFieldSpec;
    this.parameter = parameter;
  }

  public static <P extends AbstractParameter> ConvertedParameter<P> create(
      Optional<CodeBlock> mapExpr,
      Optional<CodeBlock> extractExpr,
      Skew skew,
      P parameter) {
    TypeName fieldType = parameter.returnType();
    String fieldName = '_' + parameter.enumName().enumConstant().toLowerCase(Locale.US);
    FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
    ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
    return new ConvertedParameter<>(mapExpr, extractExpr, skew, asParameterSpec,
        asFieldSpec, parameter);
  }

  public Optional<CodeBlock> mapExpr() {
    return mapExpr;
  }

  public Optional<CodeBlock> extractExpr() {
    return extractExpr;
  }

  public Skew skew() {
    return skew;
  }

  public EnumName enumName() {
    return parameter.enumName();
  }

  public boolean isRequired() {
    return skew == Skew.REQUIRED;
  }

  public boolean isRepeatable() {
    return skew == Skew.REPEATABLE;
  }

  public boolean isOptional() {
    return skew == Skew.OPTIONAL;
  }

  public boolean isFlag() {
    return skew == Skew.FLAG;
  }

  public P parameter() {
    return parameter;
  }

  public String paramLabel() {
    return parameter.paramLabel();
  }

  public String enumConstant() {
    return enumName().enumConstant();
  }

  public FieldSpec asField() {
    return asFieldSpec;
  }

  public ParameterSpec asParam() {
    return asParameterSpec;
  }
}
