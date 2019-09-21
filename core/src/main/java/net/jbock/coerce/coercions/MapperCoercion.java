package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

/**
 * Handles custom mapper.
 */
public final class MapperCoercion extends CoercionFactory {

  private final MapperType mapperType;

  private MapperCoercion(MapperType mapperType) {
    this.mapperType = mapperType;
  }

  public static Coercion create(
      Optional<AbstractCollector> collectorType,
      MapperType mapperType,
      BasicInfo basicInfo) {
    return new MapperCoercion(mapperType)
        .getCoercion(basicInfo, collectorType, Optional.of(mapperType));
  }

  @Override
  public CodeBlock createMapper(TypeMirror innerType) {
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
