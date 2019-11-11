package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static net.jbock.coerce.Util.getTypeParameterList;

public class ReferenceMapperType extends MapperType {

  private final TypeElement mapperClass; // implements Function or Supplier<Function>

  private final TypeTool tool;

  ReferenceMapperType(
      TypeTool tool,
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution) {
    super(supplier, solution);
    this.mapperClass = mapperClass;
    this.tool = tool;
  }

  @Override
  public CodeBlock mapExpr() {
    return CodeBlock.of("new $T$L()$L",
        tool.erasure(mapperClass.asType()),
        getTypeParameterList(solution()),
        supplier() ? ".get()" : "");
  }
}
