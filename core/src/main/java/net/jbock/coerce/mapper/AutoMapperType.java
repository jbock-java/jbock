package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Collections;

public class AutoMapperType extends MapperType {

  private final CodeBlock createExpression;

  private final TypeMirror innerType; // what the function returns

  AutoMapperType(TypeMirror innerType, CodeBlock createExpression) {
    super(false, Collections.emptyList(), false);
    this.createExpression = createExpression;
    this.innerType = innerType;
  }

  @Override
  public TypeMirror mapperType() {
    return TypeTool.get().stringFunction(innerType);
  }

  public TypeMirror innerType() {
    return innerType;
  }
}
