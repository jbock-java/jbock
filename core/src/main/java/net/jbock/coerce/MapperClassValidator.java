package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.either.Either;
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
import static net.jbock.coerce.Util.getTypeParameterList;
import static net.jbock.coerce.reference.ExpectedType.MAPPER;

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

  public Either<String, CodeBlock> getMapExpr() {
    commonChecks(mapperClass);
    checkNotAbstract(mapperClass);
    ReferencedType<Function<?, ?>> functionType = new ReferenceTool<>(MAPPER, errorHandler, tool, mapperClass)
        .getReferencedType();
    TypeMirror inputType = functionType.typeArguments().get(0);
    TypeMirror outputType = functionType.typeArguments().get(1);
    return tool.unify(tool.asType(String.class), inputType, this::boom)
        .flatMap(MAPPER::boom, inputSolution ->
            handle(functionType, outputType, inputSolution));
  }

  private Either<String, CodeBlock> handle(
      ReferencedType<Function<?, ?>> functionType,
      TypeMirror outputType,
      TypevarMapping inputSolution) {
    return tool.unify(expectedReturnType, outputType, this::boom)
        .flatMap(MAPPER::boom, outputSolution ->
            handle(functionType, inputSolution, outputSolution));
  }

  private Either<String, CodeBlock> handle(
      ReferencedType<Function<?, ?>> functionType,
      TypevarMapping inputSolution,
      TypevarMapping outputSolution) {
    return new Flattener(tool, mapperClass, this::boom)
        .mergeSolutions(inputSolution, outputSolution)
        .map(MAPPER::boom, typeParameters -> CodeBlock.of("new $T$L()$L",
            tool.erasure(mapperClass.asType()),
            getTypeParameterList(typeParameters.getTypeParameters()),
            functionType.isSupplier() ? ".get()" : ""));
  }

  private ValidationException boom(String message) {
    return errorHandler.apply(MAPPER.boom(message));
  }
}
