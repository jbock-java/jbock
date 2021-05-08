package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.ConvertedParameter;
import net.jbock.coerce.Skew;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.either.Either;
import net.jbock.qualifier.ConverterClass;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.Character.isWhitespace;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

class NamedOptionFactory extends ParameterScoped {

  private final BasicInfo basicInfo;
  private final ConverterClass converter;

  @Inject
  NamedOptionFactory(
      ParameterContext parameterContext,
      ConverterClass converter,
      BasicInfo basicInfo) {
    super(parameterContext);
    this.basicInfo = basicInfo;
    this.converter = converter;
  }

  Either<ValidationFailure, ConvertedParameter<NamedOption>> createNamedOption() {
    return checkOptionNames()
        .map(this::createNamedOption)
        .flatMap(namedOption -> {
          if (!converter.isPresent() && returnType().getKind() == BOOLEAN) {
            return right(createFlag(namedOption));
          }
          return basicInfo.coercion(namedOption);
        })
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }

  private Either<String, List<String>> checkOptionNames() {
    Option option = sourceMethod().getAnnotation(Option.class);
    if (option == null) {
      return right(Collections.emptyList());
    }
    if (Objects.toString(option.names(), "").isEmpty()) {
      return left("empty name");
    }
    for (ConvertedParameter<NamedOption> c : alreadyCreatedOptions()) {
      for (String name : option.names()) {
        for (String previousName : c.parameter().dashedNames()) {
          if (name.equals(previousName)) {
            return left("duplicate option name: " + name);
          }
        }
      }
    }
    List<String> result = new ArrayList<>();
    for (String name : option.names()) {
      Either<String, String> check = checkName(name);
      if (!check.isRight()) {
        return check.map(__ -> Collections.emptyList());
      }
      if (result.contains(name)) {
        return left("duplicate option name: " + name);
      }
      result.add(name);
    }
    result.sort((n1, n2) -> {
      boolean unix1 = n1.length() == 2;
      boolean unix2 = n2.length() == 2;
      if (unix1 && !unix2) {
        return -1;
      }
      if (!unix1 && unix2) {
        return 1;
      }
      return n1.compareTo(n2);
    });
    if (result.isEmpty()) {
      return left("define at least one option name");
    }
    return right(result);
  }

  private Either<String, String> checkName(String name) {
    if (Objects.toString(name, "").isEmpty()) {
      return left("empty name");
    }
    if (name.charAt(0) != '-') {
      return left("the name must start with a dash character: " + name);
    }
    if (name.startsWith("---")) {
      return left("the name must start with one or two dashes, not three:" + name);
    }
    if (name.equals("--")) {
      return left("not a valid name: --");
    }
    if (name.charAt(1) != '-' && name.length() >= 3) {
      return left("single-dash names must be single-character names: " + name);
    }
    if (flavour().helpEnabled(sourceElement())) {
      if ("--help".equals(name) || "-h".equals(name)) {
        return left("'--help' or '-h' cannot be option names, unless the help feature is disabled.");
      }
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isWhitespace(c)) {
        return left("the name contains whitespace characters: " + name);
      }
      if (c == '=') {
        return left("the name contains '=': " + name);
      }
    }
    return right(name);
  }

  private NamedOption createNamedOption(List<String> dashedNames) {
    return new NamedOption(enumName(), dashedNames, sourceMethod(), bundleKey(),
        description(), converter);
  }

  private ConvertedParameter<NamedOption> createFlag(NamedOption namedOption) {
    ParameterSpec constructorParam = ParameterSpec.builder(TypeName.get(returnType()), enumName().snake()).build();
    CodeBlock mapExpr = CodeBlock.of("$T.identity()", Function.class);
    CodeBlock tailExpr = CodeBlock.of(".findAny().isPresent()");
    CodeBlock extractExpr = CodeBlock.of("$N", constructorParam);
    return new ConvertedParameter<>(mapExpr, tailExpr, extractExpr, Skew.FLAG, constructorParam, namedOption);
  }
}
