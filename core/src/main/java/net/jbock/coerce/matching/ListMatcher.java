package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListMatcher extends Matcher {

  @Inject
  ListMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  Optional<UnwrapSuccess> tryUnwrapReturnType() {
    ParameterSpec constructorParam = constructorParam(returnType());
    return tool().getSingleTypeArgument(returnType(), List.class.getCanonicalName())
        .map(wrapped -> UnwrapSuccess.create(wrapped, constructorParam));
  }

  @Override
  NonFlagSkew skew() {
    return NonFlagSkew.REPEATABLE;
  }

  @Override
  CodeBlock tail() {
    return CodeBlock.of(".collect($T.toList())", Collectors.class);
  }
}
