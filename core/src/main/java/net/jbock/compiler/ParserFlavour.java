package net.jbock.compiler;

import net.jbock.Command;
import net.jbock.SuperCommand;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public enum ParserFlavour {

  COMMAND(Command.class) {
    @Override
    public boolean helpEnabled(TypeElement sourceElement) {
      return get(sourceElement).helpEnabled();
    }

    @Override
    public Optional<String> programName(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).name());
    }

    @Override
    public Optional<String> descriptionKey(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).descriptionKey());
    }

    @Override
    public List<String> description(TypeElement sourceElement, Elements elements) {
      String[] description = get(sourceElement).description();
      return Descriptions.getDescription(sourceElement, elements, description);
    }

    @Override
    public boolean isSuperCommand() {
      return false;
    }

    private Command get(TypeElement sourceElement) {
      return sourceElement.getAnnotation(Command.class);
    }
  },

  SUPER_COMMAND(SuperCommand.class) {
    @Override
    public boolean helpEnabled(TypeElement sourceElement) {
      return get(sourceElement).helpEnabled();
    }

    @Override
    public Optional<String> programName(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).name());
    }

    @Override
    public Optional<String> descriptionKey(TypeElement sourceElement) {
      return Descriptions.optionalString(get(sourceElement).descriptionKey());
    }

    @Override
    public List<String> description(TypeElement sourceElement, Elements elements) {
      String[] description = get(sourceElement).description();
      return Descriptions.getDescription(sourceElement, elements, description);
    }

    @Override
    public boolean isSuperCommand() {
      return true;
    }

    private SuperCommand get(TypeElement sourceElement) {
      return sourceElement.getAnnotation(SuperCommand.class);
    }
  };

  private final String className;

  ParserFlavour(Class<? extends Annotation> annotationClass) {
    this.className = annotationClass.getCanonicalName();
  }

  public static ParserFlavour forAnnotationName(String annotationName) {
    for (ParserFlavour flavour : values()) {
      if (flavour.className.equals(annotationName)) {
        return flavour;
      }
    }
    throw new IllegalArgumentException("Unknown flavour: " + annotationName);
  }

  public abstract boolean helpEnabled(TypeElement sourceElement);

  public abstract Optional<String> programName(TypeElement sourceElement);

  public abstract Optional<String> descriptionKey(TypeElement sourceElement);

  public abstract List<String> description(TypeElement sourceElement, Elements elements);

  public abstract boolean isSuperCommand();
}
