package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static net.jbock.compiler.Util.getTypeParameterList;

public class ReferenceMapperType extends MapperType {

  private final TypeElement mapperClass; // implements Function or Supplier<Function>
  private final TypeMirror innerType; // what the function returns

  ReferenceMapperType(
      TypeElement mapperClass,
      boolean supplier,
      List<TypeMirror> solution,
      TypeMirror innerType) {
    super(supplier, solution);
    this.mapperClass = mapperClass;
    this.innerType = innerType;
  }

  @Override
  public TypeMirror innerType() {
    return innerType;
  }

  @Override
  public CodeBlock mapExpr() {
    return CodeBlock.of("new $T$L()$L",
        TypeTool.get().erasure(mapperClass.asType()),
        getTypeParameterList(solution()),
        supplier() ? ".get()" : "");
  }
}
