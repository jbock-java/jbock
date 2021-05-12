package net.jbock.qualifier;

import com.squareup.javapoet.TypeName;
import net.jbock.compiler.ParserFlavour;
import net.jbock.compiler.ValidationFailure;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

public class SourceElement {

  private final TypeElement sourceElement;
  private final ParserFlavour parserFlavour;
  private final Modifier[] accessModifiers;

  public SourceElement(TypeElement sourceElement, ParserFlavour parserFlavour, Modifier[] accessModifiers) {
    this.sourceElement = sourceElement;
    this.parserFlavour = parserFlavour;
    this.accessModifiers = accessModifiers;
  }

  public static SourceElement create(TypeElement sourceElement, ParserFlavour parserFlavour) {
    Modifier[] accessModifiers = sourceElement.getModifiers().stream()
        .filter(ALLOWED_MODIFIERS::contains)
        .toArray(Modifier[]::new);
    return new SourceElement(sourceElement, parserFlavour, accessModifiers);
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
    return parserFlavour.programName(sourceElement);
  }

  public Modifier[] accessModifiers() {
    return accessModifiers;
  }
}
