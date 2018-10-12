package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;

class MapperSkew {

  final TypeMirror mapperReturnType;

  final TypeMirror baseType;

  static MapperSkew create(TypeMirror mapperReturnType, TypeMirror baseType) {
    return new MapperSkew(mapperReturnType, baseType);
  }
  
  static MapperSkew create(TypeMirror trigger) {
    return new MapperSkew(trigger, trigger);
  }

  private MapperSkew(TypeMirror mapperReturnType, TypeMirror baseType) {
    this.mapperReturnType = mapperReturnType;
    this.baseType = baseType;
  }

  boolean isSkewed() {
    return !TypeTool.get().eql(mapperReturnType, baseType);
  }
}
