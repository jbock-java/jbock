package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.INTEGER;
import static net.jbock.compiler.Constants.OPTIONAL_INT;

public enum Coercion {

  NONE {
    @Override
    public CodeBlock map() {
      return CodeBlock.builder().build();
    }

    @Override
    public boolean triggeredBy(TypeName typeName) {
      return false;
    }
  },

  INT_COERCION {
    @Override
    public CodeBlock map() {
      return CodeBlock.builder().add(".map($T::valueOf)", Integer.class).build();
    }

    @Override
    public  boolean triggeredBy(TypeName typeName) {
      return OPTIONAL_INT.equals(typeName) ||
          INT.equals(typeName) ||
          INTEGER.equals(typeName);
    }
  };

  public abstract CodeBlock map();

  public abstract boolean triggeredBy(TypeName typeName);

  public static Coercion findCoercion(TypeName typeName) {
    for (Coercion value : values()) {
      if (value.triggeredBy(typeName)) {
        return value;
      }
    }
    return NONE;
  }
}
