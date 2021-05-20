package net.jbock.qualifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.ParserFlavour;
import net.jbock.compiler.ValidationFailure;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

public class SourceElement {

  private final TypeElement sourceElement;
  private final ParserFlavour parserFlavour;
  private final Modifier[] accessModifiers;
  private final String programName;
  private final ClassName generatedClass;
  private final ClassName optionType;

  private SourceElement(
      TypeElement sourceElement,
      ParserFlavour parserFlavour,
      Modifier[] accessModifiers,
      String programName,
      ClassName generatedClass,
      ClassName optionType) {
    this.sourceElement = sourceElement;
    this.parserFlavour = parserFlavour;
    this.accessModifiers = accessModifiers;
    this.programName = programName;
    this.generatedClass = generatedClass;
    this.optionType = optionType;
  }

  public static SourceElement create(TypeElement typeElement, ParserFlavour parserFlavour) {
    Modifier[] accessModifiers = typeElement.getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .toArray(Modifier[]::new);
    String programName = parserFlavour.programName(typeElement)
        .orElseGet(() -> EnumName.create(typeElement.getSimpleName().toString()).snake('-'));
    String generatedClassName = String.join("_", ClassName.get(typeElement).simpleNames()) + "_Parser";
    ClassName generatedClass = ClassName.get(typeElement)
        .topLevelClassName()
        .peerClass(generatedClassName);
    ClassName optionType = generatedClass.nestedClass("Option");
    return new SourceElement(typeElement, parserFlavour, accessModifiers,
        programName, generatedClass, optionType);
  }

  public TypeElement element() {
    return sourceElement;
  }

  public TypeName typeName() {
    return TypeName.get(sourceElement.asType());
  }

  public ValidationFailure fail(String message) {
    return new ValidationFailure(message, sourceElement);
  }

  public boolean isSuperCommand() {
    return parserFlavour.isSuperCommand();
  }

  public boolean helpEnabled() {
    return parserFlavour.helpEnabled(sourceElement);
  }

  public String programName() {
    return programName;
  }

  public String resultMethodName() {
    return isSuperCommand() ? "getResultWithRest" : "getResult";
  }

  public Modifier[] accessModifiers() {
    return accessModifiers;
  }

  public Optional<String> descriptionKey() {
    return parserFlavour.descriptionKey(sourceElement);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public ClassName optionType() {
    return optionType;
  }

  public List<String> description(Elements elements) {
    return Arrays.asList(parserFlavour.description(sourceElement, elements));
  }
}
