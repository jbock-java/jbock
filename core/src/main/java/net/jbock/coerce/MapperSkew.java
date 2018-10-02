package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;

class MapperSkew {

  final TypeMirror mapperReturnType;

  final TypeMirror baseType;

  MapperSkew(TypeMirror mapperReturnType, TypeMirror baseType) {
    this.mapperReturnType = mapperReturnType;
    this.baseType = baseType;
  }
}
