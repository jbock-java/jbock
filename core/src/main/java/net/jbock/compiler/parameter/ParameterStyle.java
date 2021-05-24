package net.jbock.compiler.parameter;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.compiler.Descriptions;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public enum ParameterStyle {

  OPTION(Option.class) {
    @Override
    public Optional<String> descriptionKey(ExecutableElement method) {
      return Descriptions.optionalString(get(method).descriptionKey());
    }

    @Override
    public Optional<String> paramLabel(ExecutableElement method) {
      return Descriptions.optionalString(get(method).paramLabel());
    }

    @Override
    public boolean isPositional() {
      return false;
    }

    @Override
    public OptionalInt index(ExecutableElement method) {
      return OptionalInt.empty();
    }

    @Override
    public List<String> names(ExecutableElement method) {
      return List.of(get(method).names());
    }

    @Override
    public List<String> description(ExecutableElement method, Elements elements) {
      String[] description = get(method).description();
      return Descriptions.getDescription(method, elements, description);
    }

    private Option get(ExecutableElement method) {
      return method.getAnnotation(Option.class);
    }
  }, PARAMETER(Parameter.class) {
    @Override
    public Optional<String> descriptionKey(ExecutableElement method) {
      return Descriptions.optionalString(get(method).descriptionKey());
    }

    @Override
    public Optional<String> paramLabel(ExecutableElement method) {
      return Descriptions.optionalString(get(method).paramLabel());
    }

    @Override
    public boolean isPositional() {
      return true;
    }

    @Override
    public OptionalInt index(ExecutableElement method) {
      return OptionalInt.of(method.getAnnotation(Parameter.class).index());
    }

    @Override
    public List<String> names(ExecutableElement method) {
      return List.of();
    }

    @Override
    public List<String> description(ExecutableElement method, Elements elements) {
      String[] description = get(method).description();
      return Descriptions.getDescription(method, elements, description);
    }

    private Parameter get(ExecutableElement method) {
      return method.getAnnotation(Parameter.class);
    }
  }, PARAMETERS(Parameters.class) {
    @Override
    public Optional<String> descriptionKey(ExecutableElement method) {
      return Descriptions.optionalString(get(method).descriptionKey());
    }

    @Override
    public Optional<String> paramLabel(ExecutableElement method) {
      return Descriptions.optionalString(get(method).paramLabel());
    }

    @Override
    public boolean isPositional() {
      return true;
    }

    @Override
    public OptionalInt index(ExecutableElement method) {
      return OptionalInt.empty();
    }

    @Override
    public List<String> names(ExecutableElement method) {
      return List.of();
    }

    @Override
    public List<String> description(ExecutableElement method, Elements elements) {
      String[] description = get(method).description();
      return Descriptions.getDescription(method, elements, description);
    }

    private Parameters get(ExecutableElement method) {
      return method.getAnnotation(Parameters.class);
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

  public abstract Optional<String> descriptionKey(ExecutableElement method);

  public abstract Optional<String> paramLabel(ExecutableElement method);

  public abstract boolean isPositional();

  public abstract OptionalInt index(ExecutableElement method);

  public abstract List<String> names(ExecutableElement method);

  public abstract List<String> description(ExecutableElement method, Elements elements);
}
