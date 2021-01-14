package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.either.Either;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.function.Function;

abstract class Matcher extends ParameterScoped {

  Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  abstract Optional<UnwrapSuccess> tryUnwrapReturnType();

  final Either<String, MatchingSuccess> tryMatch(TypeElement mapperClass) {
    return tryUnwrapReturnType().map(unwrapSuccess -> match(mapperClass, unwrapSuccess))
        .orElse(Either.left("no match"));
  }

  final Either<String, MatchingSuccess> match(TypeElement mapperClass, UnwrapSuccess unwrapSuccess) {
    MapperClassValidator validator = new MapperClassValidator(this::failure, tool(), unwrapSuccess.wrappedType(), mapperClass);
    return validator.getMapExpr().map(Function.identity(), mapExpr -> {
      ParameterSpec constructorParam = constructorParam(unwrapSuccess.liftedType());
      return new MatchingSuccess(mapExpr, unwrapSuccess.extractExpr(constructorParam), constructorParam, skew(), autoCollectExpr());
    });
  }

  abstract NonFlagSkew skew();

  abstract CodeBlock autoCollectExpr();
}
