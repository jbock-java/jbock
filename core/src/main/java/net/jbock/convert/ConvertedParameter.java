package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.parameter.AbstractParameter;

public final class ConvertedParameter<P extends AbstractParameter> {

  private final ParameterSpec implConstructorParam;
  private final CodeBlock mapExpr;
  private final CodeBlock extractExpr;
  private final Skew skew;
  private final P parameter;
  private final EnumName enumName;
  private final FieldSpec implField;

  public ConvertedParameter(
      CodeBlock mapExpr,
      CodeBlock extractExpr,
      Skew skew,
      ParameterSpec implConstructorParam,
      EnumName enumName,
      FieldSpec implField,
      P parameter) {
    this.implConstructorParam = implConstructorParam;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.enumName = enumName;
    this.implField = implField;
    this.parameter = parameter;
  }

  public static <P extends AbstractParameter> ConvertedParameter<P> create(
      CodeBlock mapExpr,
      CodeBlock extractExpr,
      Skew skew,
      ParameterSpec implConstructorParam,
      EnumName enumName,
      P parameter) {
    TypeName fieldType = parameter.returnType();
    String fieldName = enumName.enumConstant();
    FieldSpec implField = FieldSpec.builder(fieldType, fieldName).build();
    return new ConvertedParameter<>(mapExpr, extractExpr, skew, implConstructorParam,
        enumName, implField, parameter);
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  /**
   * Converts from param type to field type.
   */
  public CodeBlock extractExpr() {
    return extractExpr;
  }

  public Skew skew() {
    return skew;
  }

  public EnumName enumName() {
    return enumName;
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
    return enumName.enumConstant();
  }

  public FieldSpec implField() {
    return implField;
  }

  public ParameterSpec implConstructorParam() {
    return implConstructorParam;
  }
}
