package net.jbock.coerce;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;

abstract class SuppliedClassValidator {

  static void commonChecks(TypeElement classToCheck, String name) throws TmpException {
    if (classToCheck.getNestingKind() == NestingKind.MEMBER &&
        !classToCheck.getModifiers().contains(Modifier.STATIC)) {
      throw TmpException.findWarning(
          String.format("The nested %s class must be static.", name));
    }
    if (classToCheck.getModifiers().contains(Modifier.PRIVATE)) {
      throw TmpException.findWarning(
          String.format("The %s class may not be private.", name));
    }
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(classToCheck.getEnclosedElements());
    checkDefaultConstructorExists(name, constructors);
  }

  private static void checkDefaultConstructorExists(String name, List<ExecutableElement> constructors) throws TmpException {
    if (constructors.isEmpty()) {
      return;
    }
    for (ExecutableElement constructor : constructors) {
      if (!constructor.getParameters().isEmpty()) {
        continue;
      }
      if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
        throw TmpException.findWarning(
            String.format("The %s class must have a package visible constructor.", name));
      }
      if (!constructor.getThrownTypes().isEmpty()) {
        throw TmpException.findWarning(
            String.format("The %s's constructor may not declare any exceptions.", name));
      }
      // constructor found
      return;
    }
    throw TmpException.findWarning(
        String.format("The %s class must have a default constructor", name));
  }
}
