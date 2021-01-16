package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.reference.FunctionType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static net.jbock.coerce.Util.getTypeParameterList;

public final class MapperClassValidator extends ParameterScoped {

  private final TypeElement mapperClass;
  private final ReferenceTool referenceTool;

  @Inject
  MapperClassValidator(
      ParameterContext parameterContext,
      TypeElement mapperClass,
      ReferenceTool referenceTool) {
    super(parameterContext);
    this.mapperClass = mapperClass;
    this.referenceTool = referenceTool;
  }

  public Either<String, CodeBlock> getMapExpr(TypeMirror expectedReturnType) {
    FunctionType functionType = referenceTool.getReferencedType().orElseThrow(this::mapperFailure);
    TypeMirror inputType = functionType.inputType();
    TypeMirror stringType = tool().asTypeElement(String.class.getCanonicalName()).asType();
    return tool().unify(stringType, inputType, this::mapperFailure)
        .flatMap(inputSolution ->
            handle(expectedReturnType, functionType, inputSolution));
  }

  private Either<String, CodeBlock> handle(
      TypeMirror expectedReturnType,
      FunctionType functionType,
      TypevarMapping inputSolution) {
    return tool().unify(expectedReturnType, functionType.outputType(), this::mapperFailure)
        .flatMap(outputSolution ->
            handle(functionType, inputSolution, outputSolution));
  }

  private Either<String, CodeBlock> handle(
      FunctionType functionType,
      TypevarMapping inputSolution,
      TypevarMapping outputSolution) {
    return inputSolution.merge(outputSolution)
        .flatMap(mapping -> mapping.getTypeParameters(mapperClass))
        .map(typeParameters -> CodeBlock.of("new $T$L()$L",
            tool().types().erasure(mapperClass.asType()),
            getTypeParameterList(typeParameters),
            functionType.isSupplier() ? ".get()" : ""));
  }
}
