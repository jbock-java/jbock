package net.jbock.compiler;

import net.jbock.Option;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Skew;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.Character.isWhitespace;

class NamedOptionFactory extends ParameterScoped {

  private final BasicInfo basicInfo;

  @Inject
  NamedOptionFactory(
      ParameterContext parameterContext,
      BasicInfo basicInfo) {
    super(parameterContext);
    this.basicInfo = basicInfo;
  }

  Parameter createNamedOption(boolean anyMnemonics) {
    checkBundleKey();
    String optionName = optionName();
    char mnemonic = mnemonic();
    Coercion coercion = basicInfo.coercion().orElseThrow(s -> ValidationException.create(sourceMethod(), s));
    List<String> dashedNames = dashedNames(optionName, mnemonic);
    return new NamedOption(mnemonic, optionName, sourceMethod(), bundleKey(),
        sample(coercion.skew(), enumName(), dashedNames, anyMnemonics),
        dashedNames, coercion, Arrays.asList(description()));
  }

  private Character mnemonic() {
    Option option = sourceMethod().getAnnotation(Option.class);
    if (option == null || option.mnemonic() == ' ') {
      return ' ';
    }
    for (Parameter param : alreadyCreated()) {
      if (option.mnemonic() == param.mnemonic()) {
        throw ValidationException.create(sourceMethod(), "Duplicate mnemonic");
      }
    }
    return checkMnemonic(option.mnemonic());
  }

  private String optionName() {
    Option option = sourceMethod().getAnnotation(Option.class);
    if (option == null) {
      return null;
    }
    if (Objects.toString(option.value(), "").isEmpty()) {
      throw ValidationException.create(sourceMethod(), "The name may not be empty");
    }
    for (Parameter param : alreadyCreated()) {
      if (option.value().equals(param.optionName())) {
        throw ValidationException.create(sourceMethod(), "Duplicate option name: " + option.value());
      }
    }
    return checkName(option.value());
  }

  private char checkMnemonic(char mnemonic) {
    if (mnemonic != ' ') {
      checkName(Character.toString(mnemonic));
    }
    return mnemonic;
  }

  private String checkName(String name) {
    if (Objects.toString(name, "").isEmpty()) {
      throw ValidationException.create(sourceMethod(), "The name may not be empty");
    }
    if (name.charAt(0) == '-') {
      throw ValidationException.create(sourceMethod(), "The name may not start with '-'");
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isWhitespace(c)) {
        throw ValidationException.create(sourceMethod(), "The name may not contain whitespace characters");
      }
      if (c == '=') {
        throw ValidationException.create(sourceMethod(), "The name may not contain '='");
      }
    }
    return name;
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
