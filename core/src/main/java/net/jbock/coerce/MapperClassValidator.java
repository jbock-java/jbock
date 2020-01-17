package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.Util.checkNotAbstract;
import static net.jbock.coerce.reference.ExpectedType.FUNCTION;

public final class MapperClassValidator {

  private final Function<String, ValidationException> errorHandler;
  private final TypeTool tool;
  private final TypeMirror expectedReturnType;
  private final TypeElement mapperClass;

  public MapperClassValidator(Function<String, ValidationException> errorHandler, TypeTool tool, TypeMirror expectedReturnType, TypeElement mapperClass) {
    this.errorHandler = errorHandler;
    this.tool = tool;
    this.expectedReturnType = expectedReturnType;
    this.mapperClass = mapperClass;
  }

  public Either<String, ReferenceMapperType> checkReturnType() {
    commonChecks(mapperClass);
    checkNotAbstract(mapperClass);
    ReferencedType<Function> functionType = new ReferenceTool<>(FUNCTION, errorHandler, tool, mapperClass).getReferencedType();
    TypeMirror inputType = functionType.typeArguments().get(0);
    TypeMirror outputType = functionType.typeArguments().get(1);
    return tool.unify(tool.asType(String.class), inputType).flatMap(FUNCTION::boom, leftSolution ->
        handle(functionType, outputType, leftSolution));
  }

  private Either<String, ReferenceMapperType> handle(ReferencedType<Function> functionType, TypeMirror outputType, TypevarMapping leftSolution) {
    return tool.unify(expectedReturnType, outputType).flatMap(FUNCTION::boom, rightSolution ->
        handle(functionType, leftSolution, rightSolution));
  }

  private Either<String, ReferenceMapperType> handle(ReferencedType<Function> functionType, TypevarMapping leftSolution, TypevarMapping rightSolution) {
    return new Flattener(errorHandler, tool, mapperClass)
        .getTypeParameters(leftSolution, rightSolution)
        .map(FUNCTION::boom, typeParameters -> ReferenceMapperType.create(tool, functionType.isSupplier(), mapperClass, typeParameters.getTypeParameters()));
  }
}
