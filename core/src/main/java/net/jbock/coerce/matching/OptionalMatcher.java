package net.jbock.coerce.matching;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.either.Either;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

class OptionalMatcher extends Matcher {

  @Inject
  OptionalMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  Either<String, MatchingSuccess> tryMatch(TypeElement mapperClass) {
    return Optionalish.unwrap(returnType(), tool()).map(opt -> {
      TypeMirror testType = opt.wrappedType();
      MapperClassValidator validator = new MapperClassValidator(this::failure, tool(), testType, mapperClass);
      return validator.getMapExpr().map(Function.identity(), mapExpr -> {
        ParameterSpec constructorParam = constructorParam(opt.liftedType());
        return new MatchingSuccess(mapExpr, opt.extractExpr(constructorParam), constructorParam, skew());
      });
    }).orElse(Either.left("no match"));
  }

  @Override
  NonFlagSkew skew() {
    return NonFlagSkew.OPTIONAL;
  }
}
