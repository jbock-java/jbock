package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.Util.getTypeParameterList;

/**
 * Handles custom mapper.
 */
public final class ExplicitMapperCoercion extends CoercionFactory {

  private final ReferenceMapperType mapperType;

  public ExplicitMapperCoercion(ReferenceMapperType mapperType) {
    this.mapperType = mapperType;
  }

  @Override
  public CodeBlock createMapper(TypeMirror innerType) {
    return CodeBlock.of("new $T$L()$L",
        TypeTool.get().erasure(mapperType.mapperType()),
        getTypeParameterList(mapperType.solution()),
        mapperType.supplier() ? ".get()" : "");
  }
}
