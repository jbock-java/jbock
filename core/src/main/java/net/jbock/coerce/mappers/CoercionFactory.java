package net.jbock.coerce.mappers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.CustomCollector;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.stream.Collector;

import static net.jbock.coerce.mappers.MapperCoercion.getTypeParameters;

public abstract class CoercionFactory {

  // trigger for this factory
  abstract TypeMirror mapperReturnType(TypeTool tool);

  /**
   * An expression that maps from String to mapperReturnType
   */
  abstract Optional<CodeBlock> mapExpr();

  CodeBlock initMapper() {
    return CodeBlock.builder().build();
  }

  public Coercion getCoercion(
      BasicInfo basicInfo,
      Optional<AbstractCollector> collector) {
    Optional<CodeBlock> mapExpr = mapExpr();
    CodeBlock initMapper = initMapper();
    TypeMirror constructorParamType = getConstructorParamType(basicInfo);
    Optional<ParameterSpec> collectorParam = collector.flatMap(collectorInfo ->
        collectorParam(basicInfo, collectorInfo));
    return Coercion.create(
        collectorParam,
        mapExpr,
        initMapper,
        mapperReturnType(basicInfo.tool()),
        collector.flatMap(this::createCollector),
        constructorParamType,
        basicInfo);
  }

  private Optional<CodeBlock> createCollector(AbstractCollector collectorInfo) {
    if (!(collectorInfo instanceof CustomCollector)) {
      return Optional.empty();
    }
    CustomCollector collector = (CustomCollector) collectorInfo;
    return Optional.of(CodeBlock.of("new $T$L",
        TypeTool.get().erasure(collector.collectorType()),
        getTypeParameters(collector.solution(), collector.supplier())));
  }

  private TypeMirror getConstructorParamType(BasicInfo basicInfo) {
    boolean useReturnType = basicInfo.isOptional() || basicInfo.isRepeatable();
    if (useReturnType) {
      return basicInfo.returnType();
    }
    return mapperReturnType(basicInfo.tool());
  }

  private static Optional<ParameterSpec> collectorParam(
      BasicInfo basicInfo,
      AbstractCollector collectorInfo) {
    TypeName t = TypeName.get(collectorInfo.inputType());
    TypeName a = WildcardTypeName.subtypeOf(Object.class);
    TypeName r = TypeName.get(basicInfo.returnType());
    return Optional.of(ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Collector.class), t, a, r),
        basicInfo.paramName() + "Collector")
        .build());
  }
}
