package net.jbock.qualifier;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

public class GeneratedType {

  private final ClassName generatedClass;
  private final ClassName optionType;

  private GeneratedType(ClassName generatedClass, ClassName optionType) {
    this.generatedClass = generatedClass;
    this.optionType = optionType;
  }

  public static GeneratedType create(TypeElement sourceElement) {
    String name = String.join("_", ClassName.get(sourceElement).simpleNames()) + "_Parser";
    ClassName generatedClass = ClassName.get(sourceElement).topLevelClassName().peerClass(name);
    ClassName optionType = generatedClass.nestedClass("Option");
    return new GeneratedType(generatedClass, optionType);
  }

  public ClassName type() {
    return generatedClass;
  }

  public ClassName optionType() {
    return optionType;
  }
}
