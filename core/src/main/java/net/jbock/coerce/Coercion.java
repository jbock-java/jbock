package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.coercions.MapperCoercion;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.CustomCollector;
import net.jbock.coerce.collector.DefaultCollector;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Coercion {

  // only for repeatable
  private final Optional<ParameterSpec> collectorParam;

  // helper.build
  private final CodeBlock mapExpr;

  // helper.build
  private final CodeBlock initMapper;

  // helper.build
  private final Optional<CodeBlock> initCollector;

  // impl constructor param
  private final ParameterSpec constructorParam;

  // impl
  private final FieldSpec field;

  private final Function<ParameterSpec, CodeBlock> extractExpr;

  Coercion(
      Optional<ParameterSpec> collectorParam,
      CodeBlock mapExpr,
      CodeBlock initMapper,
      Optional<CodeBlock> initCollector,
      ParameterSpec constructorParam,
      FieldSpec field,
      Function<ParameterSpec, CodeBlock> extractExpr) {
    this.collectorParam = collectorParam;
    this.mapExpr = mapExpr;
    this.initMapper = initMapper;
    this.initCollector = initCollector;
    this.constructorParam = constructorParam;
    this.field = field;
    this.extractExpr = extractExpr;
  }

  public static Coercion create(
      Optional<ParameterSpec> collectorParam,
      CodeBlock mapExpr,
      CodeBlock initMapper,
      Optional<AbstractCollector> collector,
      TypeMirror constructorParamType,
      BasicInfo basicInfo) {
    ParameterSpec constructorParam = ParameterSpec.builder(
        TypeName.get(constructorParamType), basicInfo.paramName()).build();
    return new Coercion(collectorParam, mapExpr,
        initMapper, collector.map(Coercion::createCollector), constructorParam, basicInfo.fieldSpec(), basicInfo.extractExpr());
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

  public Optional<CodeBlock> initCollector() {
    return initCollector;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public FieldSpec field() {
    return field;
  }

  public Optional<ParameterSpec> collectorParam() {
    return collectorParam;
  }

  public Optional<CodeBlock> collectExpr() {
    return collectorParam.map(param -> CodeBlock.of("$N", param));
  }

  public CodeBlock extractExpr() {
    return extractExpr.apply(constructorParam);
  }
}
