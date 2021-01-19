package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.reference.FunctionType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

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
    if (!tool().isSameType(functionType.inputType(), String.class.getCanonicalName())) {
      return Either.left("The function must accept an input of type String");
    }
    if (!tool().isSameType(functionType.outputType(), expectedReturnType)) {
      return Either.left("The function must return " + expectedReturnType);
    }
    return Either.right(CodeBlock.of("new $T()$L",
        tool().types().erasure(mapperClass.asType()),
        functionType.isSupplier() ? ".get()" : ""));
  }
}
