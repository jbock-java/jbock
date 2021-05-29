package net.jbock.common;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.SuperCommand;

import java.lang.annotation.Annotation;
import java.util.List;

public class Annotations {

  public static List<Class<? extends Annotation>> methodLevelAnnotations() {
    return List.of(Option.class, Parameter.class, Parameters.class);

  }
  public static List<Class<? extends Annotation>> typeLevelAnnotations() {
    return List.of(Command.class, SuperCommand.class, Converter.class);
  }
}
