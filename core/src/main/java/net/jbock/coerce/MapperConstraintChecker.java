package net.jbock.coerce;

import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

import static net.jbock.coerce.reference.ExpectedType.MAPPER;

public final class MapperConstraintChecker {

  private final BasicInfo basicInfo;
  private final TypeElement mapperClass;

  public MapperConstraintChecker(BasicInfo basicInfo, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.mapperClass = mapperClass;
  }

  public TypeMirror getConstraint() {
    ReferencedType<Function> functionType = new ReferenceTool<>(MAPPER, basicInfo, mapperClass).getReferencedType();
    TypeMirror inputType = functionType.typeArguments().get(0);
    TypeMirror outputType = functionType.typeArguments().get(1);
    return tool().unify(tool().asType(String.class), inputType)
        .orElseThrow(s -> basicInfo.asValidationException(MAPPER.boom(s)))
        .substitute(outputType)
        .orElseThrow(f -> basicInfo.asValidationException(MAPPER.boom(f.getMessage())));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
