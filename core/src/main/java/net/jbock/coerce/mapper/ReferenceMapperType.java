package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static net.jbock.coerce.Util.getTypeParameterList;

public class ReferenceMapperType extends MapperType {

  // solved typevars of mapperClass
  // this field is not used at runtime, it's used for testing only
  private final List<TypeMirror> solution;

  public ReferenceMapperType(List<TypeMirror> solution, CodeBlock mapExpr) {
    super(mapExpr);
    this.solution = solution;
  }

  public static ReferenceMapperType create(
      TypeTool tool,
      boolean supplier,
      TypeElement mapperClass,
      List<TypeMirror> solution) {
    CodeBlock mapExpr = CodeBlock.of("new $T$L()$L",
        tool.erasure(mapperClass.asType()),
        getTypeParameterList(solution),
        supplier ? ".get()" : "");
    return new ReferenceMapperType(solution, mapExpr);
  }

  public List<TypeMirror> solution() {
    return solution;
  }
}
