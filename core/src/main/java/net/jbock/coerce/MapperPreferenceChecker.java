package net.jbock.coerce;

import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypevarMapping;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
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
    FlattenerResult typeParameters = new Flattener(basicInfo, mapperClass, Optional.empty())
        .getTypeParameters(new TypevarMapping(Collections.emptyMap(), basicInfo.tool()))
        .orElseThrow(message -> basicInfo.asValidationException(MAPPER.boom(message)));
    TypeMirror result = typeParameters.resolveTypevar(outputType);
    if (outputType.getKind() == TypeKind.TYPEVAR && basicInfo.tool().isObject(result)) {
      return Optional.empty();
    }
    return Optional.of(result);
  }
}
