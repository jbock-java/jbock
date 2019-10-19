package net.jbock.coerce.reference;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class SupplierType<E> extends ReferencedType<E> {

  private final Map<String, TypeMirror> typevarMapping;

  SupplierType(Declared<E> referencedType, Map<String, TypeMirror> typevarMapping) {
    super(referencedType);
    this.typevarMapping = typevarMapping;
  }

  private String getTypevar(String typeParameter) {
    return Objects.toString(typevarMapping.get(typeParameter), typeParameter);
  }

  @Override
  public Map<String, TypeMirror> mapTypevars(Map<String, TypeMirror> solution) {
    Map<String, TypeMirror> mapped = new HashMap<>();
    for (Map.Entry<String, TypeMirror> e : solution.entrySet()) {
      mapped.put(getTypevar(e.getKey()), e.getValue());
    }
    return mapped;
  }

  @Override
  public boolean isSupplier() {
    return true;
  }
}
