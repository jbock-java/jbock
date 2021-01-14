package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.either.Either;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.function.Function;

class ListMatcher extends Matcher {

  @Inject
  ListMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  Either<String, MatchingSuccess> tryMatch(TypeElement mapperClass) {
    return tool().getSingleTypeArgument(returnType(), List.class.getCanonicalName()).map(wrapped -> {
      MapperClassValidator validator = new MapperClassValidator(this::failure, tool(), wrapped, mapperClass);
      return validator.getMapExpr().map(Function.identity(), mapExpr -> {
        ParameterSpec constructorParam = constructorParam(returnType());
        return new MatchingSuccess(mapExpr, CodeBlock.of("$N", constructorParam), constructorParam, skew());
      });
    }).orElse(Either.left("no match"));
  }

  @Override
  NonFlagSkew skew() {
    return NonFlagSkew.REPEATABLE;
  }
}
