package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.matching.Match;
import net.jbock.coerce.matching.mapper.MapperSuccess;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.function.Function;

public class Coercion {

  private final ParameterSpec constructorParam;

  private final EnumName paramName;

  private final CodeBlock tailExpr;

  private final CodeBlock mapExpr;

  private final CodeBlock extractExpr;

  private final Skew skew;

  private Coercion(
      EnumName enumName,
      CodeBlock mapExpr,
      CodeBlock tailExpr,
      CodeBlock extractExpr,
      Skew skew,
      ParameterSpec constructorParam) {
    this.constructorParam = constructorParam;
    this.paramName = enumName;
    this.tailExpr = tailExpr;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
  }

  public static Coercion create(Matcher matcher, Match unwrapSuccess, CodeBlock mapExpr) {
    CodeBlock tailExpr = matcher.tailExpr();
    CodeBlock extractExpr = unwrapSuccess.extractExpr();
    Skew skew = unwrapSuccess.skew();
    ParameterSpec constructorParam = unwrapSuccess.constructorParam();
    return new Coercion(matcher.enumName(), mapExpr, tailExpr, extractExpr, skew, constructorParam);
  }

  public static Coercion create(MapperSuccess success, EnumName enumName) {
    CodeBlock mapExpr = success.mapExpr();
    CodeBlock tailExpr = success.tailExpr();
    CodeBlock extractExpr = success.extractExpr();
    Skew skew = success.skew();
    ParameterSpec constructorParam = success.constructorParam();
    return new Coercion(enumName, mapExpr, tailExpr, extractExpr, skew, constructorParam);
  }

  public static Coercion createFlag(EnumName enumName, ExecutableElement sourceMethod) {
    ParameterSpec constructorParam = ParameterSpec.builder(TypeName.get(sourceMethod.getReturnType()), enumName.snake()).build();
    CodeBlock mapExpr = CodeBlock.of("$T.identity()", Function.class);
    CodeBlock tailExpr = CodeBlock.of(".findAny().isPresent()");
    CodeBlock extractExpr = CodeBlock.of("$N", constructorParam);
    return new Coercion(enumName, mapExpr, tailExpr, extractExpr, Skew.FLAG, constructorParam);
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public CodeBlock extractExpr() {
    return extractExpr;
  }

  public CodeBlock tailExpr() {
    return tailExpr;
  }

  public Skew skew() {
    return skew;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public EnumName paramName() {
    return paramName;
  }
}
