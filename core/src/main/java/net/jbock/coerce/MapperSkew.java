package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class MapperSkew {

  final TypeName mapperReturnType;

  final TypeName baseType;

  MapperSkew(TypeName mapperReturnType, TypeName baseType) {
    this.mapperReturnType = mapperReturnType;
    this.baseType = baseType;
  }
}
