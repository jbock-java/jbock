package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.INTEGER;
import static net.jbock.compiler.Constants.OPTIONAL_INT;

enum Coercion {

  NONE {
    @Override
    CodeBlock map() {
      return CodeBlock.builder().build();
    }

    @Override
    boolean triggeredBy(TypeName typeName) {
      return false;
    }
  },

  INT_COERCION {
    @Override
    CodeBlock map() {
      return CodeBlock.builder().add(".map($T::valueOf)", Integer.class).build();
    }

    @Override
    boolean triggeredBy(TypeName typeName) {
      return OPTIONAL_INT.equals(typeName) ||
          INT.equals(typeName) ||
          INTEGER.equals(typeName);
    }
  };

  abstract CodeBlock map();

  abstract boolean triggeredBy(TypeName typeName);

  static Coercion findCoercion(TypeName typeName) {
    for (Coercion value : values()) {
      if (value.triggeredBy(typeName)) {
        return value;
      }
    }
    return NONE;
  }
}
