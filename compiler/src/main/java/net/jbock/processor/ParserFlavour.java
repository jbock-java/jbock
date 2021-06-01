package net.jbock.processor;

import net.jbock.Command;
import net.jbock.SuperCommand;
import net.jbock.common.Descriptions;
import net.jbock.common.SafeElements;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

enum ParserFlavour {

  COMMAND(Command.class) {
    @Override
    boolean helpEnabled(TypeElement sourceElement) {
      return get(sourceElement).helpEnabled();
    }

    @Override
    Optional<String> programName(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).name());
    }

    @Override
    Optional<String> descriptionKey(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).descriptionKey());
    }

    @Override
    List<String> description(TypeElement sourceElement, SafeElements elements) {
      String[] description = get(sourceElement).description();
      return Descriptions.getDescription(sourceElement, elements, description);
    }

    @Override
    boolean expandAtSign(TypeElement sourceElement) {
      return get(sourceElement).expandAtSign();
    }

    @Override
    boolean isSuperCommand() {
      return false;
    }

    @Override
    boolean isAnsi(TypeElement sourceElement) {
      return get(sourceElement).ansi();
    }

    private Command get(TypeElement sourceElement) {
      return sourceElement.getAnnotation(Command.class);
    }
  },

  SUPER_COMMAND(SuperCommand.class) {
    @Override
    boolean helpEnabled(TypeElement sourceElement) {
      return get(sourceElement).helpEnabled();
    }

    @Override
    Optional<String> programName(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).name());
    }

    @Override
    Optional<String> descriptionKey(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).descriptionKey());
    }

    @Override
    List<String> description(TypeElement sourceElement, SafeElements elements) {
      String[] description = get(sourceElement).description();
      return Descriptions.getDescription(sourceElement, elements, description);
    }

    @Override
    boolean expandAtSign(TypeElement sourceElement) {
      return get(sourceElement).expandAtSign();
    }

    @Override
    boolean isSuperCommand() {
      return true;
    }

    @Override
    boolean isAnsi(TypeElement sourceElement) {
      return get(sourceElement).ansi();
    }

    private SuperCommand get(TypeElement sourceElement) {
      return sourceElement.getAnnotation(SuperCommand.class);
    }
  };

  private final String className;

  ParserFlavour(Class<? extends Annotation> annotationClass) {
    this.className = annotationClass.getCanonicalName();
  }

  static ParserFlavour forAnnotationName(String annotationName) {
    for (ParserFlavour flavour : values()) {
      if (flavour.className.equals(annotationName)) {
        return flavour;
      }
    }
    throw new IllegalArgumentException("Unknown flavour: " + annotationName);
  }

  abstract boolean isSuperCommand();

  abstract boolean helpEnabled(TypeElement sourceElement);

  abstract Optional<String> programName(TypeElement sourceElement);

  abstract Optional<String> descriptionKey(TypeElement sourceElement);

  abstract List<String> description(TypeElement sourceElement, SafeElements elements);

  abstract boolean expandAtSign(TypeElement sourceElement);

  abstract boolean isAnsi(TypeElement sourceElement);
}
