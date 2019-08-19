package net.jbock.coerce.mappers;

import com.squareup.javapoet.*;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.compiler.Constants.STRING;

public final class MapperCoercion extends CoercionFactory {

  private final ParameterSpec mapperParam;

  private final TypeMirror mapperType;

  private MapperCoercion(TypeMirror mapperReturnType, ParameterSpec mapperParam, TypeMirror mapperType) {
    super(mapperReturnType);
    this.mapperParam = mapperParam;
    this.mapperType = mapperType;
  }

  public static Coercion create(
      TypeMirror mapperReturnType,
      Optional<TypeMirror> collectorType,
      ParameterSpec mapperParam,
      TypeMirror mapperType,
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
    return CodeBlock.of("$T $N = new $T().get()",
        ParameterizedTypeName.get(ClassName.get(Function.class), STRING, TypeName.get(mapperReturnType)),
        mapperParam,
        mapperType);
  }
}
