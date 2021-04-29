package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Skew;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.either.Either;
import net.jbock.qualifier.MapperClass;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Character.isWhitespace;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

class NamedOptionFactory extends ParameterScoped {

  private final BasicInfo basicInfo;
  private final boolean mapperPresent;

  @Inject
  NamedOptionFactory(
      ParameterContext parameterContext,
      @MapperClass Optional<TypeElement> mapperClass,
      BasicInfo basicInfo) {
    super(parameterContext);
    this.basicInfo = basicInfo;
    this.mapperPresent = mapperClass.isPresent();
  }

  Either<ValidationFailure, Coercion<NamedOption>> createNamedOption() {
    return checkFullName()
        .flatMap(optionName -> mnemonic().map(mnemonic -> new OptionNames(optionName, mnemonic)))
        .map(this::createNamedOption)
        .flatMap(namedOption -> {
          if (!mapperPresent && returnType().getKind() == BOOLEAN) {
            return right(createFlag(namedOption));
          }
          return basicInfo.coercion(namedOption);
        })
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }

  private Either<String, Character> mnemonic() {
    Option option = sourceMethod().getAnnotation(Option.class);
    if (option == null || option.mnemonic() == ' ') {
      return right(' ');
    }
    for (Coercion<NamedOption> c : alreadyCreatedOptions()) {
      if (option.mnemonic() == c.parameter().mnemonic()) {
        return left("duplicate mnemonic");
      }
    }
    return checkMnemonic(option.mnemonic());
  }

  private Either<String, String> checkFullName() {
    Option option = sourceMethod().getAnnotation(Option.class);
    if (option == null) {
      return right("");
    }
    if (Objects.toString(option.value(), "").isEmpty()) {
      return left("empty name");
    }
    if (isHelpEnabled() && "help".equals(option.value())) {
      return left("'help' cannot be an option name, unless the help feature is disabled. " +
          "The help feature can be disabled by setting @Command.helpDisabled = true.");
    }
    for (Coercion<NamedOption> c : alreadyCreatedOptions()) {
      if (option.value().equals(c.parameter().optionName())) {
        return left("duplicate option name");
      }
    }
    return checkName(option.value());
  }

  private Either<String, Character> checkMnemonic(char mnemonic) {
    if (mnemonic != ' ') {
      return checkName(Character.toString(mnemonic))
          .map(s -> s.charAt(0));
    }
    return right(' ');
  }

  private Either<String, String> checkName(String name) {
    if (Objects.toString(name, "").isEmpty()) {
      return left("empty name");
    }
    if (name.charAt(0) == '-') {
      return left("name starts with '-'");
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isWhitespace(c)) {
        return left("name contains whitespace characters");
      }
      if (c == '=') {
        return left("name contains '='");
      }
    }
    return right(name);
  }

  private NamedOption createNamedOption(OptionNames names) {
    String optionName = names.optionName;
    Character mnemonic = names.mnemonic;
    return new NamedOption(mnemonic, optionName, sourceMethod(), bundleKey(),
        Arrays.asList(description()));
  }

  private Coercion<NamedOption> createFlag(NamedOption namedOption) {
    ParameterSpec constructorParam = ParameterSpec.builder(TypeName.get(returnType()), enumName().snake()).build();
    CodeBlock mapExpr = CodeBlock.of("$T.identity()", Function.class);
    CodeBlock tailExpr = CodeBlock.of(".findAny().isPresent()");
    CodeBlock extractExpr = CodeBlock.of("$N", constructorParam);
    return new Coercion<>(enumName(), mapExpr, tailExpr, extractExpr, Skew.FLAG, constructorParam, namedOption);
  }
}
