package net.jbock.coerce.matching;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.AutoMapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.NonFlagCoercion;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

public class AutoMatcher extends ParameterScoped {

  private static final String ENUM = Enum.class.getCanonicalName();

  private final AutoMapper autoMapper;
  private final ImmutableList<Matcher> matchers;

  @Inject
  AutoMatcher(ParameterContext context, AutoMapper autoMapper, OptionalMatcher optionalMatcher, ListMatcher listMatcher, ExactMatcher exactMatcher) {
    super(context);
    this.autoMapper = autoMapper;
    this.matchers = ImmutableList.of(optionalMatcher, listMatcher, exactMatcher);
  }

  public Coercion findCoercion() {
    return tryFindCoercion()
        .flatMap(entry -> {
          Matcher matcher = entry.getKey();
          UnwrapSuccess unwrapSuccess = entry.getValue();
          ParameterSpec constructorParam = constructorParam(unwrapSuccess.constructorParamType());
          return findMapExpr(unwrapSuccess.wrappedType()).map(mapExpr ->
              new NonFlagCoercion(enumName(), mapExpr, matcher.autoCollectExpr(),
                  unwrapSuccess.extractExpr(constructorParam), matcher.skew(), constructorParam));
        })
        .orElseThrow(() -> failure(String.format("Unknown parameter type: %s. Try defining a custom mapper or collector.",
            returnType())));
  }

  private Optional<Map.Entry<Matcher, UnwrapSuccess>> tryFindCoercion() {
    for (Matcher matcher : matchers) {
      Optional<UnwrapSuccess> success = matcher.tryUnwrapReturnType();
      if (success.isPresent()) {
        return success.map(e -> new AbstractMap.SimpleImmutableEntry<>(matcher, e));
      }
    }
    return Optional.empty();
  }

  private Optional<CodeBlock> findMapExpr(TypeMirror unwrappedReturnType) {
    Optional<CodeBlock> mapExpr = autoMapper.findAutoMapper(unwrappedReturnType);
    if (mapExpr.isPresent()) {
      return mapExpr;
    }
    if (isEnumType(unwrappedReturnType)) {
      return Optional.of(CodeBlock.of("$T::valueOf", unwrappedReturnType));
    }
    return Optional.empty();
  }

  private boolean isEnumType(TypeMirror type) {
    return types().directSupertypes(type).stream()
        .anyMatch(t -> tool().isSameErasure(t, ENUM));
  }
}
