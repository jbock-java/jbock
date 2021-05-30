package net.jbock.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.common.EnumName;
import net.jbock.common.SafeElements;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public class SourceElement {

  private final TypeElement sourceElement;
  private final ParserFlavour parserFlavour;
  private final List<Modifier> accessModifiers;
  private final String programName;
  private final ClassName generatedClass;
  private final ClassName itemType;

  private SourceElement(
      TypeElement sourceElement,
      ParserFlavour parserFlavour,
      List<Modifier> accessModifiers,
      String programName,
      ClassName generatedClass,
      ClassName itemType) {
    this.sourceElement = sourceElement;
    this.parserFlavour = parserFlavour;
    this.accessModifiers = accessModifiers;
    this.programName = programName;
    this.generatedClass = generatedClass;
    this.itemType = itemType;
  }

  static SourceElement create(TypeElement typeElement, ParserFlavour parserFlavour) {
    List<Modifier> accessModifiers = typeElement.getModifiers().stream()
        .filter(ACCESS_MODIFIERS::contains)
        .collect(Collectors.toUnmodifiableList());
    String programName = parserFlavour.programName(typeElement)
        .orElseGet(() -> EnumName.create(typeElement.getSimpleName().toString()).snake('-'));
    String generatedClassName = String.join("_", ClassName.get(typeElement).simpleNames()) + "_Parser";
    ClassName generatedClass = ClassName.get(typeElement)
        .topLevelClassName()
        .peerClass(generatedClassName);
    ClassName itemType = generatedClass.nestedClass("Item");
    return new SourceElement(typeElement, parserFlavour, accessModifiers,
        programName, generatedClass, itemType);
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

  public boolean isAnsi() {
    return parserFlavour.isAnsi(sourceElement);
  }

  public String programName() {
    return programName;
  }

  public String resultMethodName() {
    return isSuperCommand() ? "getResultWithRest" : "getResult";
  }

  public List<Modifier> accessModifiers() {
    return accessModifiers;
  }

  public Optional<String> descriptionKey() {
    return parserFlavour.descriptionKey(sourceElement);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public ClassName itemType() {
    return itemType;
  }

  public boolean isInterface() {
    return sourceElement.getKind() == ElementKind.INTERFACE;
  }

  public List<String> description(SafeElements elements) {
    return parserFlavour.description(sourceElement, elements);
  }
}
