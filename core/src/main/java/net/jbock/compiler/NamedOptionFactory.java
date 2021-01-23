package net.jbock.compiler;

import net.jbock.Option;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Skew;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.either.Either;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.Character.isWhitespace;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

class NamedOptionFactory extends ParameterScoped {

  private final BasicInfo basicInfo;

  @Inject
  NamedOptionFactory(
      ParameterContext parameterContext,
      BasicInfo basicInfo) {
    super(parameterContext);
    this.basicInfo = basicInfo;
  }

  Either<ValidationFailure, ? extends Parameter> createNamedOption(boolean anyMnemonics) {
    return Either.<String, Void>fromOptionalFailure(checkBundleKey())
        .select(this::optionName)
        .select(optionName -> mnemonic().map(mnemonic -> new Names(optionName, mnemonic)))
        .select(names -> basicInfo.coercion()
            .map(coercion -> {
              String optionName = names.optionName;
              Character mnemonic = names.mnemonic;
              List<String> dashedNames = dashedNames(optionName, mnemonic);
              return new NamedOption(mnemonic, optionName, sourceMethod(), bundleKey(),
                  sample(coercion.skew(), enumName(), dashedNames, anyMnemonics),
                  dashedNames, coercion, Arrays.asList(description()));
            }))
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }

  private static class Names {
    final String optionName;
    final Character mnemonic;

    Names(String optionName, Character mnemonic) {
      this.optionName = optionName;
      this.mnemonic = mnemonic;
    }
  }

  private Either<String, Character> mnemonic() {
    Option option = sourceMethod().getAnnotation(Option.class);
    if (option == null || option.mnemonic() == ' ') {
      return right(' ');
    }
    for (Parameter param : alreadyCreated()) {
      if (option.mnemonic() == param.mnemonic()) {
        throw ValidationException.create(sourceMethod(), "Duplicate mnemonic");
      }
    }
    return checkMnemonic(option.mnemonic());
  }

  private Either<String, String> optionName() {
    Option option = sourceMethod().getAnnotation(Option.class);
    if (option == null) {
      return right("");
    }
    if (Objects.toString(option.value(), "").isEmpty()) {
      return left("empty name");
    }
    for (Parameter param : alreadyCreated()) {
      if (option.value().equals(param.optionName())) {
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

  private static List<String> dashedNames(String optionName, char mnemonic) {
    if (optionName != null && mnemonic == ' ') {
      return Collections.singletonList("--" + optionName);
    } else if (optionName == null && mnemonic != ' ') {
      return Collections.singletonList("-" + mnemonic);
    } else if (optionName == null) {
      return Collections.emptyList();
    }
    return Arrays.asList("-" + mnemonic, "--" + optionName);
  }

  private static String sample(Skew skew, EnumName name, List<String> names, boolean anyMnemonics) {
    if (names.isEmpty() || names.size() >= 3) {
      throw new AssertionError();
    }
    String argname = skew == Skew.FLAG ? "" : ' ' + name.enumConstant();
    if (names.size() == 1) {
      // Note: The padding has the same length as the string "-f, "
      return (anyMnemonics ? "    " : "") + names.get(0) + argname;
    }
    return names.get(0) + ", " + names.get(1) + argname;
  }
}
