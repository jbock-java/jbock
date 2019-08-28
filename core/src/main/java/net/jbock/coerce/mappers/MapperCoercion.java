package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CollectorType;
import net.jbock.coerce.MapperType;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class MapperCoercion extends CoercionFactory {

  private final ParameterSpec mapperParam;

  private final MapperType mapperType;

  private MapperCoercion(
      TypeMirror mapperReturnType,
      ParameterSpec mapperParam,
      MapperType mapperType) {
    super(mapperReturnType);
    this.mapperParam = mapperParam;
    this.mapperType = mapperType;
  }

  public static Coercion create(
      TypeMirror mapperReturnType,
      Optional<CollectorType> collectorType,
      ParameterSpec mapperParam,
      MapperType mapperType,
      BasicInfo basicInfo) {
    return new MapperCoercion(mapperReturnType, mapperParam, mapperType)
        .getCoercion(basicInfo, collectorType);
  }

  @Override
  public Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$N", mapperParam));
  }

  @Override
  public CodeBlock initMapper() {
    return CodeBlock.of(String.format("$T $N = new $T%s()%s",
        this.mapperType.hasTypeParams() ? "" : "", // TODO "<>"
        this.mapperType.supplier() ? ".get()" : ""),
        mapperParam.type,
        mapperParam,
        this.mapperType.mapperType());
  }
}
