package net.jbock.coerce;

import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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
    TypeMirror outputType = functionType.typeArguments().get(1);
    if (outputType.getKind() == TypeKind.DECLARED) {
      DeclaredType declared = TypeTool.asDeclared(outputType); // TODO validFreeTypevarsInMapperAndCollectorMapperPreferencePossibleIntegerToNumber
    }
    if (outputType.getKind() != TypeKind.TYPEVAR) {
      return outputType;
    }
    return basicInfo.tool().getBound(findByName(outputType.toString()))
        .orElseThrow(s -> basicInfo.asValidationException(MAPPER.boom(s)));
  }

  private TypeParameterElement findByName(String name) {
    for (TypeParameterElement p : mapperClass.getTypeParameters()) {
      if (p.toString().equals(name)) {
        return p;
      }
    }
    throw new AssertionError("expecting a type parameter named " + name);
  }
}
