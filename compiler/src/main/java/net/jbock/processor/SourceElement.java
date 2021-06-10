package net.jbock.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.Command;
import net.jbock.common.Descriptions;
import net.jbock.common.EnumName;
import net.jbock.common.SafeElements;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public class SourceElement {

  private final TypeElement sourceElement;
  private final List<Modifier> accessModifiers;
  private final String programName;
  private final ClassName generatedClass;
  private final ClassName optionEnumType;

  private SourceElement(
      TypeElement sourceElement,
      List<Modifier> accessModifiers,
      String programName,
      ClassName generatedClass,
      ClassName optionEnumType) {
    this.sourceElement = sourceElement;
    this.accessModifiers = accessModifiers;
    this.programName = programName;
    this.generatedClass = generatedClass;
    this.optionEnumType = optionEnumType;
  }

  static SourceElement create(TypeElement typeElement) {
    List<Modifier> accessModifiers = typeElement.getModifiers().stream()
        .filter(ACCESS_MODIFIERS::contains)
        .collect(Collectors.toUnmodifiableList());
    String programName = programName(typeElement)
        .orElseGet(() -> EnumName.create(typeElement.getSimpleName().toString()).snake('-'));
    String generatedClassName = String.join("_", ClassName.get(typeElement).simpleNames()) + "Parser";
    ClassName generatedClass = ClassName.get(typeElement)
        .topLevelClassName()
        .peerClass(generatedClassName);
    ClassName optionEnumType = generatedClass.nestedClass("Opt");
    return new SourceElement(typeElement, accessModifiers,
        programName, generatedClass, optionEnumType);
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
    return get().superCommand();
  }

  public boolean helpEnabled() {
    return get().helpEnabled();
  }

  public boolean atFileExpansion() {
    return get().atFileExpansion();
  }

  public List<Modifier> accessModifiers() {
    return accessModifiers;
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public ClassName optionEnumType() {
    return optionEnumType;
  }

  public boolean isInterface() {
    return sourceElement.getKind() == ElementKind.INTERFACE;
  }

  private static Optional<String> programName(TypeElement sourceElement) {
    Command command = sourceElement.getAnnotation(Command.class);
    return Descriptions.optionalString(command.name());
  }

  public String programName() {
    return programName;
  }

  public Optional<String> descriptionKey() {
    return Descriptions.optionalString(get().descriptionKey());
  }

  public List<String> description(SafeElements elements) {
    String[] description = get().description();
    return Descriptions.getDescription(sourceElement, elements, description);
  }

  public boolean isAnsi() {
    return get().ansi();
  }

  public boolean unixClustering() {
    return get().unixClustering();
  }

  private Command get() {
    return sourceElement.getAnnotation(Command.class);
  }
}
