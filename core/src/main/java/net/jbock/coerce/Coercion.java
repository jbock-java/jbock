package net.jbock.coerce;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.jbock.coerce.coercions.MapperCoercion;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.CustomCollector;
import net.jbock.coerce.collector.DefaultCollector;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Coercion {

  private final Optional<CollectorInfo> collectorInfo;

  // helper.build
  private final CodeBlock mapExpr;

  // helper.build
  private final CodeBlock initMapper;

  // impl constructor param
  private final ParameterSpec constructorParam;

  // impl
  private final FieldSpec field;

  private final Function<ParameterSpec, CodeBlock> extractExpr;

  private final boolean optional;

  Coercion(
      Optional<CollectorInfo> collectorInfo,
      CodeBlock mapExpr,
      CodeBlock initMapper,
      ParameterSpec constructorParam,
      FieldSpec field,
      Function<ParameterSpec, CodeBlock> extractExpr,
      boolean optional) {
    this.collectorInfo = collectorInfo;
    this.mapExpr = mapExpr;
    this.initMapper = initMapper;
    this.constructorParam = constructorParam;
    this.field = field;
    this.extractExpr = extractExpr;
    this.optional = optional;
  }

  public static Coercion create(
      CodeBlock mapExpr,
      CodeBlock initMapper,
      Optional<AbstractCollector> collector,
      TypeMirror constructorParamType,
      BasicInfo basicInfo,
      boolean optional) {
    ParameterSpec constructorParam = ParameterSpec.builder(
        TypeName.get(constructorParamType), basicInfo.paramName()).build();
    return new Coercion(collector.map(c -> {
      ParameterSpec p = collectorParam(basicInfo, c);
      return new CollectorInfo(Coercion.createCollector(c), p,
          CodeBlock.of("$N", p));
    }), mapExpr,
        initMapper, constructorParam, basicInfo.fieldSpec(), basicInfo.extractExpr(), optional);
  }

  private static ParameterSpec collectorParam(
      BasicInfo basicInfo,
      AbstractCollector collectorInfo) {
    TypeName t = TypeName.get(collectorInfo.inputType());
    TypeName a = WildcardTypeName.subtypeOf(Object.class);
    TypeName r = TypeName.get(basicInfo.returnType());
    return ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Collector.class), t, a, r),
        basicInfo.paramName() + "Collector")
        .build();
  }

  private static CodeBlock createCollector(AbstractCollector collectorInfo) {
    if (collectorInfo instanceof DefaultCollector) {
      return CodeBlock.of("$T.toList()", Collectors.class);
    }
    CustomCollector collector = (CustomCollector) collectorInfo;
    return CodeBlock.of("new $T$L",
        TypeTool.get().erasure(collector.collectorType()),
        MapperCoercion.getTypeParameters(collector.solution(), collector.supplier()));
  }

  public static class CollectorInfo {

    private final CodeBlock initCollector;
    private final ParameterSpec collectorParam;
    private final CodeBlock collectExpr;

    public CollectorInfo(CodeBlock initCollector, ParameterSpec collectorParam, CodeBlock collectExpr) {
      this.initCollector = initCollector;
      this.collectorParam = collectorParam;
      this.collectExpr = collectExpr;
    }

    public CodeBlock initCollector() {
      return initCollector;
    }

    public ParameterSpec collectorParam() {
      return collectorParam;
    }

    public CodeBlock collectExpr() {
      return collectExpr;
    }
  }

  /**
   * Maps from String to mapperReturnType
   * @return an expression
   */
  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public CodeBlock initMapper() {
    return initMapper;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public FieldSpec field() {
    return field;
  }

  public CodeBlock extractExpr() {
    return extractExpr.apply(constructorParam);
  }

  public Optional<CollectorInfo> collectorInfo() {
    return collectorInfo;
  }

  public Optional<Boolean> optional() {
    return Optional.of(optional);
  }
}
