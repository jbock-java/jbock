package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.NonFlagCoercion;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.NonFlagSkew.OPTIONAL;
import static net.jbock.coerce.NonFlagSkew.REPEATABLE;
import static net.jbock.coerce.NonFlagSkew.REQUIRED;

public class AutoMatcher extends ParameterScoped {

  @Inject
  AutoMatcher(ParameterContext context) {
    super(context);
  }

  public Coercion findCoercion() {
    TypeMirror returnType = returnType();
    Optional<Optionalish> opt = Optionalish.unwrap(returnType, tool());
    Optional<TypeMirror> listWrapped = tool().getSingleTypeArgument(returnType, List.class.getCanonicalName());
    if (opt.isPresent()) {
      Optionalish optional = opt.get();
      // optional match
      ParameterSpec param = constructorParam(optional.liftedType());
      return createCoercion(optional.wrappedType(), optional.extractExpr(param), param, OPTIONAL);
    }
    if (listWrapped.isPresent()) {
      // repeatable match
      ParameterSpec param = constructorParam(returnType);
      return createCoercion(listWrapped.get(), param, REPEATABLE);
    }
    // exact match (-> required)
    ParameterSpec param = constructorParam(returnType);
    return createCoercion(boxedReturnType(), param, REQUIRED);
  }

  private NonFlagCoercion createCoercion(TypeMirror testType, ParameterSpec constructorParam, NonFlagSkew skew) {
    return createCoercion(testType, CodeBlock.of("$N", constructorParam), constructorParam, skew);
  }

  private NonFlagCoercion createCoercion(TypeMirror testType, CodeBlock extractExpr, ParameterSpec constructorParam, NonFlagSkew skew) {
    return findAutoMapper(testType)
        .map(mapExpr -> new NonFlagCoercion(enumName(), mapExpr, MatchingAttempt.autoCollectExpr(optionType(), enumName(), skew), extractExpr, skew, constructorParam))
        .orElseThrow(() -> failure(String.format("Unknown parameter type: %s. Try defining a custom mapper or collector.",
            returnType())));
  }
}
