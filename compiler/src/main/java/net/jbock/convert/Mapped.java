package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.common.EnumName;
import net.jbock.parameter.AbstractItem;
import net.jbock.model.Skew;

import java.util.Locale;
import java.util.Optional;

public final class Mapped<P extends AbstractItem> {

  private final CodeBlock mapExpr;
  private final Optional<CodeBlock> extractExpr;
  private final Skew skew;
  private final P item;
  private final ParameterSpec asParameterSpec;
  private final FieldSpec asFieldSpec;

  private Mapped(
      CodeBlock mapExpr,
      Optional<CodeBlock> extractExpr,
      Skew skew,
      ParameterSpec asParameterSpec,
      FieldSpec asFieldSpec,
      P item) {
    this.asParameterSpec = asParameterSpec;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.asFieldSpec = asFieldSpec;
    this.item = item;
  }

  public static <P extends AbstractItem> Mapped<P> create(
      CodeBlock mapExpr,
      Optional<CodeBlock> extractExpr,
      Skew skew,
      P parameter) {
    TypeName fieldType = parameter.returnType();
    String fieldName = '_' + parameter.enumName().enumConstant().toLowerCase(Locale.US);
    FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
    ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
    return new Mapped<>(mapExpr, extractExpr, skew, asParameterSpec,
        asFieldSpec, parameter);
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public Optional<CodeBlock> extractExpr() {
    return extractExpr;
  }

  public Skew skew() {
    return skew;
  }

  public EnumName enumName() {
    return item.enumName();
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
    return skew == Skew.MODAL_FLAG;
  }

  public P item() {
    return item;
  }

  public String paramLabel() {
    return item.paramLabel();
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
