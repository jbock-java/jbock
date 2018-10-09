package net.jbock.coerce;

import net.jbock.compiler.Util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

class CollectorClassValidator {

  static TypeMirror findInput(TypeElement collectorClass) {
    MapperClassValidator.commonChecks(collectorClass, "collector");
    TypeMirror supplierInterface = collectorClass.getInterfaces().get(0);
    DeclaredType parameterized = Util.asParameterized(supplierInterface);
    TypeMirror collectorInterface = parameterized.getTypeArguments().get(0);
    DeclaredType collectorInterfaceParameterized = Util.asParameterized(collectorInterface);
    return collectorInterfaceParameterized.getTypeArguments().get(0);
    // todo more validations
  }

  static TypeMirror findCollectorInterface() {
    return null;
  }
}
