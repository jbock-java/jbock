package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.Util.checkNotAbstract;
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
    commonChecks(mapperClass);
    checkNotAbstract(mapperClass);
    ReferencedType<Function> functionType = new ReferenceTool<>(MAPPER, basicInfo, mapperClass).getReferencedType();
    TypeMirror inputType = functionType.typeArguments().get(0);
    TypeMirror outputType = functionType.typeArguments().get(1);
    return tool().unify(tool().asType(String.class), inputType).flatMap(MAPPER::boom, leftSolution ->
        handle(functionType, outputType, leftSolution));
  }

  private Either<String, ReferenceMapperType> handle(ReferencedType<Function> functionType, TypeMirror outputType, TypevarMapping leftSolution) {
    return tool().unify(expectedReturnType, outputType).flatMap(MAPPER::boom, rightSolution ->
        handle(functionType, leftSolution, rightSolution));
  }

  private Either<String, ReferenceMapperType> handle(ReferencedType<Function> functionType, TypevarMapping leftSolution, TypevarMapping rightSolution) {
    return new Flattener(basicInfo, mapperClass, Optional.empty())
        .getTypeParameters(leftSolution, rightSolution)
        .map(MAPPER::boom, typeParameters ->
            MapperType.create(tool(), functionType.isSupplier(), mapperClass, typeParameters.getTypeParameters()));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
