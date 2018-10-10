package net.jbock.coerce;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;

abstract class SuppliedClassValidator {

  static void commonChecks(TypeElement classToCheck, String name) throws TmpException {
    if (classToCheck.getNestingKind() == NestingKind.MEMBER && !classToCheck.getModifiers().contains(Modifier.STATIC)) {
      throw TmpException.create(
          String.format("The nested %s class must be static", name));
    }
    if (classToCheck.getModifiers().contains(Modifier.PRIVATE)) {
      throw TmpException.create(
          String.format("The %s class may not be private", name));
    }
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(classToCheck.getEnclosedElements());
    if (!constructors.isEmpty()) {
      boolean constructorFound = false;
      for (ExecutableElement constructor : constructors) {
        if (constructor.getParameters().isEmpty()) {
          if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
            throw TmpException.create(
                String.format("The %s class must have a package visible constructor", name));
          }
          if (!constructor.getThrownTypes().isEmpty()) {
            throw TmpException.create(
                String.format("The %s's constructor may not declare any exceptions", name));
          }
          constructorFound = true;
        }
      }
      if (!constructorFound) {
        throw TmpException.create(
            String.format("The %s class must have a default constructor", name));
      }
    }
  }
}
