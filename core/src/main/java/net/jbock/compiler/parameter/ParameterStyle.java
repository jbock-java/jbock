package net.jbock.compiler.parameter;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.OptionalInt;

public enum ParameterStyle {

  OPTION(Option.class) {
    @Override
    public String descriptionKey(ExecutableElement method) {
      return get(method).map(Option::descriptionKey).orElse("");
    }

    @Override
    public String paramLabel(ExecutableElement method) {
      return get(method).map(Option::paramLabel).orElse("");
    }

    @Override
    public boolean isPositional() {
      return false;
    }

    @Override
    public OptionalInt index(ExecutableElement method) {
      return OptionalInt.empty();
    }

    private Optional<Option> get(ExecutableElement method) {
      return Optional.ofNullable(method.getAnnotation(Option.class));
    }
  }, PARAMETER(Parameter.class) {
    @Override
    public String descriptionKey(ExecutableElement method) {
      return get(method).map(Parameter::descriptionKey).orElse("");
    }

    @Override
    public String paramLabel(ExecutableElement method) {
      return get(method).map(Parameter::paramLabel).orElse("");
    }

    @Override
    public boolean isPositional() {
      return true;
    }

    @Override
    public OptionalInt index(ExecutableElement method) {
      return OptionalInt.of(method.getAnnotation(Parameter.class).index());
    }

    private Optional<Parameter> get(ExecutableElement method) {
      return Optional.ofNullable(method.getAnnotation(Parameter.class));
    }
  }, PARAMETERS(Parameters.class) {
    @Override
    public String descriptionKey(ExecutableElement method) {
      return get(method).map(Parameters::descriptionKey).orElse("");
    }

    @Override
    public String paramLabel(ExecutableElement method) {
      return get(method).map(Parameters::paramLabel).orElse("");
    }

    @Override
    public boolean isPositional() {
      return true;
    }

    @Override
    public OptionalInt index(ExecutableElement method) {
      return OptionalInt.empty();
    }

    private Optional<Parameters> get(ExecutableElement method) {
      return Optional.ofNullable(method.getAnnotation(Parameters.class));
    }
  };

  private final Class<? extends Annotation> annotationClass;

  ParameterStyle(Class<? extends Annotation> annotationClass) {
    this.annotationClass = annotationClass;
  }

  public static ParameterStyle getStyle(ExecutableElement sourceMethod) {
    for (ParameterStyle style : values()) {
      if (sourceMethod.getAnnotation(style.annotationClass) != null) {
        return style;
      }
    }
    throw new IllegalArgumentException("no style: " + sourceMethod.getSimpleName());
  }

  public abstract String descriptionKey(ExecutableElement method);

  public abstract String paramLabel(ExecutableElement method);

  public abstract boolean isPositional();

  public abstract OptionalInt index(ExecutableElement method);
}
