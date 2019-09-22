package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * Handles custom mapper.
 */
public final class ExplicitMapperCoercion extends CoercionFactory {

  private final MapperType mapperType;

  public ExplicitMapperCoercion(MapperType mapperType) {
    this.mapperType = mapperType;
  }

  @Override
  public CodeBlock createMapper(TypeMirror innerType) {
    return CodeBlock.of("new $T$L()$L",
        TypeTool.get().erasure(mapperType.mapperType()),
        getTypeParameters(mapperType.solution()),
        mapperType.supplier() ? ".get()" : "");
  }

  public static CodeBlock getTypeParameters(
      List<TypeMirror> params) {
    if (params.isEmpty()) {
      return CodeBlock.of("");
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
    code.add(">");
    return code.build();
  }
}
