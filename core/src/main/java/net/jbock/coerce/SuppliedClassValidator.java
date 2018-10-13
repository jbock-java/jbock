package net.jbock.coerce;

import net.jbock.compiler.Util;

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
    if (!Util.checkDefaultConstructorExists(classToCheck)) {
      throw TmpException.findWarning(
          String.format("The %s class must have a default constructor", name));
    }
  }
}
