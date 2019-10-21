package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.reference.ExpectedType.MAPPER;

public final class MapperClassValidator {

  private final BasicInfo basicInfo;
  private final TypeMirror expectedReturnType;
  private final TypeElement mapperClass;

  public MapperClassValidator(BasicInfo basicInfo, TypeMirror expectedReturnType, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.expectedReturnType = expectedReturnType;
    this.mapperClass = mapperClass;
  }

  public Either<String, ReferenceMapperType> checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    ReferencedType<Function> functionType = new ReferenceTool<>(MAPPER, basicInfo, mapperClass)
        .getReferencedType();
    TypeMirror t = functionType.typeArguments().get(0);
    TypeMirror r = functionType.typeArguments().get(1);
    Optional<Map<String, TypeMirror>> t_result = tool().unify(tool().asType(String.class), t);
    if (!t_result.isPresent()) {
      return failure(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    Optional<Map<String, TypeMirror>> r_result = tool().unify(expectedReturnType, r);
    if (!r_result.isPresent()) {
      return failure(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return new Flattener(basicInfo, mapperClass)
        .getTypeParameters(Arrays.asList(t_result.get(), r_result.get()))
        .map(MAPPER::boom,
            typeParameters -> MapperType.create(tool(), functionType.isSupplier(),
                mapperClass, typeParameters));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private Either<String, ReferenceMapperType> failure(String message) {
    return Either.left(MAPPER.boom(message));
  }
}
