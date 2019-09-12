package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.MapperType;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

/**
 * Handles custom mapper.
 */
public final class MapperCoercion extends CoercionFactory {

  private final ParameterSpec mapperParam;

  private final MapperType mapperType;

  private final TypeMirror mapperReturnType;

  private MapperCoercion(
      TypeMirror mapperReturnType,
      ParameterSpec mapperParam,
      MapperType mapperType) {
    this.mapperReturnType = mapperReturnType;
    this.mapperParam = mapperParam;
    this.mapperType = mapperType;
  }

  public static Coercion create(
      TypeMirror mapperReturnType,
      Optional<AbstractCollector> collectorType,
      ParameterSpec mapperParam,
      MapperType mapperType,
      BasicInfo basicInfo) {
    return new MapperCoercion(mapperReturnType, mapperParam, mapperType)
        .getCoercion(basicInfo, collectorType);
  }

  @Override
  TypeMirror mapperReturnType(TypeTool tool) {
    return mapperReturnType;
  }

  @Override
  public Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$N", mapperParam));
  }

  @Override
  public CodeBlock initMapper() {
    return CodeBlock.of("$T $N = $L",
        mapperParam.type,
        mapperParam,
        createMapper());
  }

  private CodeBlock createMapper() {
    return CodeBlock.of("new $T$L",
        TypeTool.get().erasure(mapperType.mapperType()),
        getTypeParameters(mapperType.solution(), mapperType.supplier()));
  }

  public static CodeBlock getTypeParameters(
      List<TypeMirror> params,
      boolean supplier) {
    if (params.isEmpty()) {
      if (!supplier) {
        // new X();
        return CodeBlock.of("()");
      } else {
        // new X().get();
        return CodeBlock.of("().get()");
      }
    }
    if (!supplier) {
      // new X<>();
      return CodeBlock.of("<>()");
    }
    CodeBlock.Builder code = CodeBlock.builder();
    // compiler can't handle new X<>().get();
    // needs explicit type params
    code.add("<");
    for (int i = 0; i < params.size(); i++) {
      TypeMirror typeMirror = params.get(i);
      code.add("$T", typeMirror == null ? Object.class : typeMirror);
      if (i < params.size() - 1) {
        code.add(", ");
      }
    }
    code.add(">().get()");
    return code.build();
  }
}
