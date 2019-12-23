package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.reference.ExpectedType.MAPPER;

public final class MapperPreferenceChecker {

  private final BasicInfo basicInfo;
  private final TypeElement mapperClass;

  public MapperPreferenceChecker(BasicInfo basicInfo, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.mapperClass = mapperClass;
  }

  public Optional<TypeMirror> getPreference() {
    ReferencedType<Function> functionType = new ReferenceTool<>(MAPPER, basicInfo, mapperClass).getReferencedType();
    TypeMirror outputType = functionType.typeArguments().get(1);
    if (outputType.getKind() != TypeKind.TYPEVAR) {
      return Optional.of(outputType);
    }
    Either<String, TypeMirror> bound = basicInfo.tool().getBound(findByName(outputType.toString()));
    return Optional.of(bound.orElseThrow(s -> basicInfo.asValidationException(MAPPER.boom(s))));
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
