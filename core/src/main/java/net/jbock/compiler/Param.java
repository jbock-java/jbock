package net.jbock.compiler;

import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;
import static net.jbock.compiler.Util.equalsType;

import java.util.Objects;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import net.jbock.ArgumentName;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.LongName;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

final class Param {

  private final String longName;
  private final String shortName;
  private final OptionType optionType;

  private final String stopword;

  final ExecutableElement variableElement;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Param(
      String shortName,
      String longName,
      String stopword,
      ExecutableElement variableElement) {
    this.shortName = shortName;
    this.longName = longName;
    this.stopword = stopword;
    this.variableElement = variableElement;
    this.optionType = getOptionType(variableElement);
  }

  private static OptionType getOptionType(ExecutableElement executableElement) {
    if (executableElement.getAnnotation(OtherTokens.class) != null) {
      return OptionType.OTHER_TOKENS;
    }
    if (executableElement.getAnnotation(EverythingAfter.class) != null) {
      return OptionType.EVERYTHING_AFTER;
    }
    TypeMirror type = executableElement.getReturnType();
    if (type.getKind() == TypeKind.BOOLEAN) {
      return OptionType.FLAG;
    }
    if (isListOfString(type)) {
      return OptionType.REPEATABLE;
    }
    if (isOptionalString(type)) {
      return OptionType.OPTIONAL;
    }
    String message = "Only Optional<String>, List<String> and boolean allowed, " +
        String.format("but %s() returns %s", executableElement.getSimpleName(), type);
    throw new ValidationException(message, executableElement);
  }

  static Param create(ExecutableElement parameter) {
    CreateHelper createHelper = new CreateHelper(parameter);
    if (parameter.getAnnotation(OtherTokens.class) != null) {
      return createHelper.createOtherTokens();
    }
    if (parameter.getAnnotation(EverythingAfter.class) != null) {
      return createHelper.createEverythingAfter();
    }
    String longName = longName(parameter);
    String shortName = shortName(parameter);
    createHelper.checkName(shortName);
    createHelper.checkName(longName);
    return new Param(
        shortName,
        longName,
        null,
        parameter);
  }

  private static String shortName(ExecutableElement parameter) {
    ShortName shortName = parameter.getAnnotation(ShortName.class);
    return shortName != null ? Character.toString(shortName.value()) : null;
  }

  private static String longName(ExecutableElement parameter) {
    LongName longName = parameter.getAnnotation(LongName.class);
    String ln = longName != null ? longName.value() : null;
    if (ln == null && parameter.getAnnotation(ShortName.class) == null) {
      return parameter.getSimpleName().toString();
    }
    return ln;
  }

  private static void checkList(ExecutableElement variableElement) {
    if (!isListOfString(variableElement.getReturnType())) {
      throw new ValidationException("Must be a List<String>", variableElement);
    }
  }

  private static boolean isListOfString(TypeMirror type) {
    return isXOfString(type, "java.util.List");
  }

  private static boolean isOptionalString(TypeMirror type) {
    return isXOfString(type, "java.util.Optional");
  }

  private static boolean isXOfString(
      TypeMirror type, String x) {
    DeclaredType declared = type.accept(Util.AS_DECLARED, null);
    if (declared == null) {
      return false;
    }
    if (declared.getTypeArguments().size() != 1) {
      return false;
    }
    TypeElement element = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    return x.equals(
        element.getQualifiedName().toString()) &&
        equalsType(declared.getTypeArguments().get(0),
            "java.lang.String");
  }

  private static final class CreateHelper {
    final ExecutableElement parameter;

    CreateHelper(ExecutableElement parameter) {
      this.parameter = parameter;
    }

    private void checkName(String name) {
      if (name == null) {
        return;
      }
      basicCheckName(name);
      if (name.startsWith("-")) {
        throw new ValidationException("The name may not start with '-'", parameter);
      }
      if (name.indexOf('=') >= 0) {
        throw new ValidationException("The name may not contain '='", parameter);
      }
    }

    private void basicCheckName(String name) {
      if (name == null) {
        throw new ValidationException("The name may not be null", parameter);
      }
      if (name.isEmpty()) {
        throw new ValidationException("The name may not be empty", parameter);
      }
      if (WHITE_SPACE.matcher(name).matches()) {
        throw new ValidationException("The name may not contain whitespace characters", parameter);
      }
    }

    private Param createEverythingAfter() {
      checkList(parameter);
      if (parameter.getAnnotation(LongName.class) != null) {
        throw new ValidationException(
            "@EverythingAfter and @LongName cannot be on the same parameter", parameter);
      }
      if (parameter.getAnnotation(ShortName.class) != null) {
        throw new ValidationException(
            "@EverythingAfter and @ShortName cannot be on the same parameter", parameter);
      }
      String stopword = parameter.getAnnotation(EverythingAfter.class).value();
      basicCheckName(stopword);
      return new Param(null,
          parameter.getSimpleName().toString(),
          stopword,
          parameter);
    }

    private Param createOtherTokens() {
      if (parameter.getAnnotation(EverythingAfter.class) != null) {
        throw new ValidationException(
            "@OtherTokens and @EverythingAfter cannot be on the same parameter", parameter);
      }
      checkList(parameter);
      if (parameter.getAnnotation(LongName.class) != null) {
        throw new ValidationException(
            "@OtherTokens and @LongName cannot be on the same parameter", parameter);
      }
      if (parameter.getAnnotation(ShortName.class) != null) {
        throw new ValidationException(
            "@OtherTokens and @ShortName cannot be on the same parameter", parameter);
      }
      return new Param(null,
          parameter.getSimpleName().toString(),
          null,
          parameter);
    }
  }


  String shortName() {
    return Objects.toString(shortName, null);
  }

  String longName() {
    return longName;
  }

  String stopword() {
    return stopword;
  }

  Description description() {
    return variableElement.getAnnotation(Description.class);
  }

  ArgumentName argName() {
    return variableElement.getAnnotation(ArgumentName.class);
  }

  String parameterName() {
    return variableElement.getSimpleName().toString();
  }

  OptionType optionType() {
    return optionType;
  }
}
