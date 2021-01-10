package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.coerce.Util.getTypeParameterList;
import static net.jbock.coerce.reference.ExpectedType.MAPPER;

public final class MapperClassValidator {

  private static final List<String> SPECIAL_TYPES = Stream.of(Optional.class, List.class)
      .map(Class::getCanonicalName)
      .collect(Collectors.toList());

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
    ReferencedType<Function<?, ?>> functionType = new ReferenceTool<>(MAPPER, this::boom, tool, mapperClass)
        .getReferencedType();
    TypeMirror inputType = functionType.typeArguments().get(0);
    TypeMirror outputType = functionType.typeArguments().get(1);
    for (String specialType : SPECIAL_TYPES) {
      if (tool.isSameErasure(outputType, specialType)) {
        throw boom("The mapper must not return one of the special types " + SPECIAL_TYPES);
      }
    }
    return tool.unify(tool.asTypeElement(String.class.getCanonicalName()).asType(), inputType, this::boom)
        .flatMap(Function.identity(), inputSolution ->
            handle(functionType, outputType, inputSolution));
  }

  private Either<String, CodeBlock> handle(
      ReferencedType<Function<?, ?>> functionType,
      TypeMirror outputType,
      TypevarMapping inputSolution) {
    return tool.unify(expectedReturnType, outputType, this::boom)
        .flatMap(Function.identity(), outputSolution ->
            handle(functionType, inputSolution, outputSolution));
  }

  private Either<String, CodeBlock> handle(
      ReferencedType<Function<?, ?>> functionType,
      TypevarMapping inputSolution,
      TypevarMapping outputSolution) {
    return inputSolution.merge(outputSolution)
        .flatMap(Function.identity(), mapping ->
            mapping.getTypeParameters(mapperClass))
        .map(Function.identity(), typeParameters -> CodeBlock.of("new $T$L()$L",
            tool.types().erasure(mapperClass.asType()),
            getTypeParameterList(typeParameters),
            functionType.isSupplier() ? ".get()" : ""));
  }

  private ValidationException boom(String message) {
    return errorHandler.apply(message);
  }
}
