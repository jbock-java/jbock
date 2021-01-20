package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.TypeTool;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

class Optionalish {

  private final TypeTool tool;
  private final EnumName enumName;

  @Inject
  Optionalish(TypeTool tool, EnumName enumName) {
    this.tool = tool;
    this.enumName = enumName;
  }

  Either<String, UnwrapSuccess> unwrap(TypeMirror type) {
    Either<String, UnwrapSuccess> optionalPrimitive = getOptionalPrimitive(type);
    if (optionalPrimitive.isPresent()) {
      return optionalPrimitive;
    }
    ParameterSpec constructorParam = constructorParam(type);
    return tool.getSingleTypeArgument(type, Optional.class.getCanonicalName())
        .map(wrapped -> UnwrapSuccess.create(wrapped, constructorParam, 1));
  }

  private Either<String, UnwrapSuccess> getOptionalPrimitive(TypeMirror type) {
    for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
      if (tool.isSameType(type, optionalPrimitive.type())) {
        ParameterSpec constructorParam = constructorParam(asOptional(optionalPrimitive));
        return right(UnwrapSuccess.create(
            tool.asTypeElement(optionalPrimitive.wrappedObjectType()).asType(),
            constructorParam,
            1,
            optionalPrimitive.extractExpr(constructorParam)));
      }
    }
    return left();
  }

  private DeclaredType asOptional(OptionalPrimitive optionalPrimitive) {
    TypeElement optional = tool.asTypeElement(Optional.class.getCanonicalName());
    TypeElement element = tool.asTypeElement(optionalPrimitive.wrappedObjectType());
    return tool.types().getDeclaredType(optional, element.asType());
  }

  private ParameterSpec constructorParam(TypeMirror constructorParamType) {
    return ParameterSpec.builder(TypeName.get(constructorParamType), enumName.camel()).build();
  }
}
