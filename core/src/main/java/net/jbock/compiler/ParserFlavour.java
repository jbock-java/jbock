package net.jbock.compiler;

import net.jbock.Command;
import net.jbock.SuperCommand;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

public enum ParserFlavour {

  COMMAND(Command.class) {
    @Override
    public boolean helpEnabled(TypeElement sourceElement) {
      return sourceElement.getAnnotation(Command.class).helpEnabled();
    }

    @Override
    public String programName(TypeElement sourceElement) {
      Command command = sourceElement.getAnnotation(Command.class);
      if (!command.name().isEmpty()) {
        return command.name();
      }
      return EnumName.create(sourceElement.getSimpleName().toString()).snake('-');
    }

    @Override
    public boolean isSuperCommand() {
      return false;
    }
  },

  SUPER_COMMAND(SuperCommand.class) {
    @Override
    public boolean helpEnabled(TypeElement sourceElement) {
      return sourceElement.getAnnotation(SuperCommand.class).helpEnabled();
    }

    @Override
    public String programName(TypeElement sourceElement) {
      SuperCommand command = sourceElement.getAnnotation(SuperCommand.class);
      if (!command.name().isEmpty()) {
        return command.name();
      }
      return EnumName.create(sourceElement.getSimpleName().toString()).snake('-');
    }

    @Override
    public boolean isSuperCommand() {
      return true;
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

  public abstract String programName(TypeElement sourceElement);

  public abstract boolean isSuperCommand();
}
