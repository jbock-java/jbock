package net.jbock.coerce.reference;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SupplierType extends AbstractReferencedType {

  private final Map<String, TypeMirror> typevarMapping;

  public SupplierType(DeclaredType referencedType, Map<String, TypeMirror> typevarMapping) {
    super(referencedType);
    this.typevarMapping = typevarMapping;
  }

  public static Map<String, TypeMirror> createTypevarMapping(
      List<? extends TypeMirror> typeArguments,
      List<? extends TypeParameterElement> typeParameters) {
    Map<String, TypeMirror> mapping = new HashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      TypeParameterElement p = typeParameters.get(i);
      TypeMirror typeArgument = typeArguments.get(i);
      if (typeArgument.getKind() == TypeKind.TYPEVAR) {
        mapping.put(p.toString(), typeArgument);
      }
    }
    return mapping;
  }

  @Override
  public String getTypevar(String typeParameter) {
    return Objects.toString(typevarMapping.get(typeParameter), typeParameter);
  }
}
