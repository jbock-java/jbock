package net.jbock.compiler;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.SuperCommand;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

class DescriptionBuilder {

  private final Elements elements;

  @Inject
  DescriptionBuilder(Elements elements) {
    this.elements = elements;
  }

  Description getDescription(Element el) {
    String[] description = getDescriptionFromAttribute(el);
    if (description.length == 0) {
      String[] javadoc = tokenizeJavadoc(elements.getDocComment(el));
      return new Description(javadoc);
    }
    return new Description(description);
  }

  private String[] getDescriptionFromAttribute(Element el) {
    Option option = el.getAnnotation(Option.class);
    if (option != null) {
      return option.description();
    }
    Parameter parameter = el.getAnnotation(Parameter.class);
    if (parameter != null) {
      return parameter.description();
    }
    Parameters parameters = el.getAnnotation(Parameters.class);
    if (parameters != null) {
      return parameters.description();
    }
    Command command = el.getAnnotation(Command.class);
    if (command != null) {
      return command.description();
    }
    SuperCommand superCommand = el.getAnnotation(SuperCommand.class);
    if (superCommand != null) {
      return superCommand.description();
    }
    return new String[0];
  }

  private static String[] tokenizeJavadoc(String docComment) {
    if (docComment == null) {
      return new String[0];
    }
    String[] tokens = docComment.trim().split("\\R", -1);
    List<String> result = new ArrayList<>(tokens.length);
    for (String t : tokens) {
      String token = t.trim();
      if (token.startsWith("@")) {
        return result.toArray(new String[0]);
      }
      if (!token.isEmpty()) {
        result.add(token);
      }
    }
    return result.toArray(new String[0]);
  }
}
