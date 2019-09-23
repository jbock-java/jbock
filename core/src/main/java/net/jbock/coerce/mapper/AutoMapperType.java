package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.coercions.CoercionFactory;

import javax.lang.model.type.TypeMirror;
import java.util.Collections;

public class AutoMapperType extends MapperType {

  private final CoercionFactory factory;

  private final TypeMirror innerType; // what the function returns

  AutoMapperType(TypeMirror innerType, CoercionFactory factory, boolean optional) {
    super(false, Collections.emptyList(), optional);
    this.factory = factory;
    this.innerType = innerType;
  }

  @Override
  public TypeMirror innerType() {
    return innerType;
  }

  @Override
  public CodeBlock mapExpr() {
    return factory.mapExpr();
  }
}
