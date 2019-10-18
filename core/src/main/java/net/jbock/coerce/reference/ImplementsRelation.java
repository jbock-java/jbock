package net.jbock.coerce.reference;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.TypeTool.asDeclared;

/**
 * "Dog implements Animal"
 * Any free type parameters in Animal also appear in Dog.
 */
public class ImplementsRelation {

  private final TypeElement dog;
  private final DeclaredType animal;

  public ImplementsRelation(TypeElement dog, TypeMirror animal) {
    this.dog = dog;
    this.animal = asDeclared(animal);
  }

  TypeElement dog() {
    return dog;
  }

  DeclaredType animal() {
    return animal;
  }
}
