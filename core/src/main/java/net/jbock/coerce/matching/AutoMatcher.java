package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.AutoMapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.NonFlagCoercion;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.NonFlagSkew.OPTIONAL;
import static net.jbock.coerce.NonFlagSkew.REPEATABLE;
import static net.jbock.coerce.NonFlagSkew.REQUIRED;

public class AutoMatcher extends ParameterScoped {

  private final AutoMapper autoMapper;

  @Inject
  AutoMatcher(ParameterContext context, AutoMapper autoMapper) {
    super(context);
    this.autoMapper = autoMapper;
  }

  public Coercion findCoercion() {
    TypeMirror returnType = returnType();
    Optional<Optionalish> opt = Optionalish.unwrap(returnType, tool());
    Optional<TypeMirror> listWrapped = tool().getSingleTypeArgument(returnType, List.class.getCanonicalName());
    if (opt.isPresent()) {
      Optionalish optional = opt.get();
      // optional match
      ParameterSpec param = constructorParam(optional.liftedType());
      return createCoercion(OPTIONAL, optional.wrappedType(), optional.extractExpr(param), param);
    }
    if (listWrapped.isPresent()) {
      // repeatable match
      ParameterSpec param = constructorParam(returnType);
      return createCoercion(REPEATABLE, listWrapped.get(), CodeBlock.of("$N", param), param);
    }
    // exact match (-> required)
    ParameterSpec param = constructorParam(returnType);
    return createCoercion(REQUIRED, boxedReturnType(), CodeBlock.of("$N", param), param);
  }

  private NonFlagCoercion createCoercion(
      NonFlagSkew skew,
      TypeMirror unwrappedReturnType,
      CodeBlock extractExpr,
      ParameterSpec constructorParam) {
    return findAutoMapper(unwrappedReturnType)
        .map(mapExpr -> new NonFlagCoercion(enumName(), mapExpr, MatchingAttempt.autoCollectExpr(optionType(), enumName(), skew), extractExpr, skew, constructorParam))
        .orElseThrow(() -> failure(String.format("Unknown parameter type: %s. Try defining a custom mapper or collector.",
            returnType())));
  }

  private Optional<CodeBlock> findAutoMapper(TypeMirror unwrappedReturnType) {
    Optional<CodeBlock> mapExpr = autoMapper.findAutoMapper(unwrappedReturnType);
    if (mapExpr.isPresent()) {
      return mapExpr;
    }
    if (isEnumType(unwrappedReturnType)) {
      return Optional.of(CodeBlock.of("$T::valueOf", unwrappedReturnType));
    }
    return Optional.empty();
  }

  private boolean isEnumType(TypeMirror mirror) {
    Types types = tool().types();
    return types.directSupertypes(mirror).stream()
        .anyMatch(t -> tool().isSameErasure(t, Enum.class.getCanonicalName()));
  }
}
