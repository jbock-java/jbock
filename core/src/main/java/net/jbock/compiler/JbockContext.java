package net.jbock.compiler;

import static net.jbock.compiler.LessElements.asType;

import java.util.List;
import javax.lang.model.element.TypeElement;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

public final class JbockContext {

  public final TypeElement sourceType;
  public final ClassName generatedClass;
  public final List<Param> parameters;
  public final String stopword;
  public final boolean otherTokens;
  public final boolean rest;

  private JbockContext(
      TypeElement sourceType,
      ClassName generatedClass,
      List<Param> parameters,
      String stopword, boolean otherTokens, boolean rest) {
    this.sourceType = sourceType;
    this.generatedClass = generatedClass;
    this.parameters = parameters;
    this.stopword = stopword;
    this.otherTokens = otherTokens;
    this.rest = rest;
  }

  static JbockContext create(
      TypeElement sourceType,
      List<Param> parameters,
      String stopword) {
    ClassName generatedClass = peer(ClassName.get(asType(sourceType)), Processor.SUFFIX);
    boolean otherTokens = parameters.stream()
        .anyMatch(p -> p.optionType == OptionType.OTHER_TOKENS);
    boolean rest = parameters.stream()
        .anyMatch(p -> p.optionType == OptionType.EVERYTHING_AFTER);
    return new JbockContext(sourceType, generatedClass, parameters, stopword, otherTokens, rest);
  }

  private static ClassName peer(ClassName type, String suffix) {
    String name = String.join("_", type.simpleNames()) + suffix;
    return type.topLevelClassName().peerClass(name);
  }

  public TypeName returnType() {
    return TypeName.get(sourceType.asType());
  }
}
